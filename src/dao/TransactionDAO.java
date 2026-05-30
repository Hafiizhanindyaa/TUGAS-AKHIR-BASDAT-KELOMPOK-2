package dao;

import config.DBConnection;
import model.Pesanan;
import model.DetailPesanan;
import model.Voucher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // =========================================================================
    // 1. MANAJEMEN VOUCHER & VALIDASI (READ & UPDATE)
    // =========================================================================

    // READ: Mengambil data voucher berdasarkan kodenya untuk dicek saat checkout
    public Voucher getVoucherByKode(String kodeVoucher) {
        String sql = "SELECT * FROM VOUCHER WHERE Kode_Voucher = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, kodeVoucher);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Voucher(
                        rs.getString("Kode_Voucher"),
                        rs.getString("Tipe_Diskon"),
                        rs.getDouble("Nilai_Diskon"),
                        rs.getDouble("Minimum_Belanja"),
                        rs.getDate("Tanggal_Mulai"),
                        rs.getDate("Tanggal_Berakhir"),
                        rs.getInt("Kuota_Pemakaian")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================================
    // 2. PROSES TRANSAKSI CHECKOUT LAMA (DIPERTAHANKAN, TIDAK DIPAKAI LAGI)
    //    Method ini sudah tidak dipanggil oleh CheckoutPanel.
    //    Digantikan oleh insertTransaksiLengkapDenganSaldoZalora() di bawah.
    // =========================================================================

    /**
     * @deprecated Gunakan insertTransaksiLengkapDenganSaldoZalora() sebagai gantinya.
     *             Method ini dipertahankan agar tidak merusak kode lama.
     */
    public boolean insertTransaksiLengkap(Pesanan pesanan, List<DetailPesanan> listDetail) {
        String sqlPesanan     = "INSERT INTO PESANAN (ID_Pesanan, Waktu_Transaksi, Total_Harga_Barang, Biaya_Pengiriman, Total_Tagihan, Status_Pesanan, Label_Alamat, ID_Pelanggan, ID_Metode, ID_Kurir, Kode_Voucher) VALUES (?, GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetail      = "INSERT INTO DETAIL_PESANAN (ID_Pesanan, ID_Varian, Subtotal, Jumlah_Beli, Harga_Saat_Beli) VALUES (?, ?, ?, ?, ?)";
        String sqlUpdateStok  = "UPDATE VARIAN_PRODUK SET Stok = Stok - ? WHERE ID_Varian = ?";
        String sqlUpdateVouch = "UPDATE VOUCHER SET Kuota_Pemakaian = Kuota_Pemakaian - 1 WHERE Kode_Voucher = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psPesanan = conn.prepareStatement(sqlPesanan)) {
                psPesanan.setInt(1, pesanan.getIdPesanan());
                psPesanan.setDouble(2, pesanan.getTotalHargaBarang());
                psPesanan.setDouble(3, pesanan.getBiayaPengiriman());
                psPesanan.setDouble(4, pesanan.getTotalTagihan());
                psPesanan.setString(5, pesanan.getStatusPesanan());
                psPesanan.setString(6, pesanan.getLabelAlamat());
                psPesanan.setInt(7, pesanan.getIdPelanggan());
                psPesanan.setInt(8, pesanan.getIdMetode());
                psPesanan.setInt(9, pesanan.getIdKurir());
                if (pesanan.getKodeVoucher() != null && !pesanan.getKodeVoucher().trim().isEmpty()) {
                    psPesanan.setString(10, pesanan.getKodeVoucher());
                } else {
                    psPesanan.setNull(10, Types.VARCHAR);
                }
                psPesanan.executeUpdate();
            }

            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement psStok   = conn.prepareStatement(sqlUpdateStok)) {
                for (DetailPesanan det : listDetail) {
                    psDetail.setInt(1, pesanan.getIdPesanan());
                    psDetail.setInt(2, det.getIdVarian());
                    psDetail.setDouble(3, det.getSubtotal());
                    psDetail.setInt(4, det.getJumlahBeli());
                    psDetail.setDouble(5, det.getHargaSaatBeli());
                    psDetail.executeUpdate();

                    psStok.setInt(1, det.getJumlahBeli());
                    psStok.setInt(2, det.getIdVarian());
                    psStok.executeUpdate();
                }
            }

            if (pesanan.getKodeVoucher() != null && !pesanan.getKodeVoucher().trim().isEmpty()) {
                try (PreparedStatement psVouch = conn.prepareStatement(sqlUpdateVouch)) {
                    psVouch.setString(1, pesanan.getKodeVoucher());
                    psVouch.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { System.err.println("Transaksi gagal! Rollback..."); conn.rollback(); }
                catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // =========================================================================
    // 3. CHECKOUT BARU: HANYA MEMAKAI SALDO ZALORA (UTAMA)
    //    Method ini menggantikan insertTransaksiLengkap() untuk checkout pesanan.
    //    Semua proses berada dalam SATU transaction SQL yang atomic.
    // =========================================================================

    /**
     * Memproses checkout pesanan menggunakan Saldo Zalora secara atomik.
     *
     * Urutan operasi dalam SATU transaction:
     * 1. Lock baris pelanggan & ambil saldo (UPDLOCK agar aman dari race condition)
     * 2. Cek apakah saldo >= total tagihan
     *    - Jika TIDAK cukup: rollback, return false (tidak ada data yang berubah)
     * 3. INSERT pesanan ke tabel PESANAN
     * 4. INSERT semua detail ke tabel DETAIL_PESANAN
     * 5. UPDATE stok varian berkurang sesuai jumlah beli
     * 6. UPDATE kuota voucher berkurang 1 (jika voucher digunakan)
     * 7. UPDATE saldo pelanggan berkurang sebesar total tagihan
     * 8. INSERT riwayat saldo dengan Jenis_Mutasi = 'PEMBAYARAN'
     * 9. COMMIT semua perubahan
     *
     * Jika terjadi error di langkah mana pun → ROLLBACK semua langkah.
     * Saldo TIDAK dikurangi di CheckoutPanel, hanya di sini.
     *
     * @param pesanan    Object pesanan yang sudah diisi lengkap
     * @param listDetail List detail item belanjaan
     * @return true jika checkout berhasil, false jika saldo tidak cukup atau error
     */
    public boolean insertTransaksiLengkapDenganSaldoZalora(Pesanan pesanan, List<DetailPesanan> listDetail) {

        // SQL untuk ambil saldo dengan UPDLOCK (SQL Server row-level lock)
        // Mencegah dua checkout berjalan bersamaan yang bisa menyebabkan saldo minus
        String sqlCekSaldo = "SELECT Saldo_Zalora FROM PELANGGAN WITH (UPDLOCK, ROWLOCK) WHERE ID_Pelanggan = ?";

        String sqlPesanan = "INSERT INTO PESANAN "
                + "(ID_Pesanan, Waktu_Transaksi, Total_Harga_Barang, Biaya_Pengiriman, "
                + " Total_Tagihan, Status_Pesanan, Label_Alamat, ID_Pelanggan, ID_Metode, ID_Kurir, Kode_Voucher) "
                + "VALUES (?, GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlDetail = "INSERT INTO DETAIL_PESANAN "
                + "(ID_Pesanan, ID_Varian, Subtotal, Jumlah_Beli, Harga_Saat_Beli) "
                + "VALUES (?, ?, ?, ?, ?)";

        String sqlUpdateStok    = "UPDATE VARIAN_PRODUK SET Stok = Stok - ? WHERE ID_Varian = ?";
        String sqlUpdateVoucher = "UPDATE VOUCHER SET Kuota_Pemakaian = Kuota_Pemakaian - 1 WHERE Kode_Voucher = ?";
        String sqlPotongSaldo   = "UPDATE PELANGGAN SET Saldo_Zalora = Saldo_Zalora - ? WHERE ID_Pelanggan = ?";

        String sqlInsertRiwayat = "INSERT INTO RIWAYAT_SALDO_ZALORA "
                + "(ID_Mutasi, ID_Pelanggan, Jenis_Mutasi, Nominal, "
                + " Saldo_Sebelum, Saldo_Sesudah, Waktu_Mutasi, Keterangan, ID_Pesanan, ID_Metode) "
                + "VALUES (?, ?, 'PEMBAYARAN', ?, ?, ?, GETDATE(), ?, ?, NULL)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // === MULAI TRANSACTION ===

            // -------------------------------------------------------
            // LANGKAH 1: Ambil saldo pelanggan dengan lock
            // -------------------------------------------------------
            double saldoSebelum = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlCekSaldo)) {
                ps.setInt(1, pesanan.getIdPelanggan());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        saldoSebelum = rs.getDouble("Saldo_Zalora");
                    } else {
                        System.err.println("Checkout gagal: Pelanggan ID " + pesanan.getIdPelanggan() + " tidak ditemukan.");
                        conn.rollback();
                        return false;
                    }
                }
            }

            // -------------------------------------------------------
            // LANGKAH 2: Validasi saldo
            // -------------------------------------------------------
            double totalTagihan = pesanan.getTotalTagihan();
            if (saldoSebelum < totalTagihan) {
                System.err.println("Checkout gagal: Saldo tidak cukup. "
                        + "Saldo: Rp " + saldoSebelum
                        + " | Dibutuhkan: Rp " + totalTagihan);
                conn.rollback(); // Tidak ada perubahan data
                return false;
            }

            // -------------------------------------------------------
            // LANGKAH 3: INSERT pesanan ke tabel PESANAN
            // -------------------------------------------------------
            try (PreparedStatement ps = conn.prepareStatement(sqlPesanan)) {
                ps.setInt(1, pesanan.getIdPesanan());
                ps.setDouble(2, pesanan.getTotalHargaBarang());
                ps.setDouble(3, pesanan.getBiayaPengiriman());
                ps.setDouble(4, pesanan.getTotalTagihan());
                ps.setString(5, pesanan.getStatusPesanan()); // "Diproses"
                ps.setString(6, pesanan.getLabelAlamat());
                ps.setInt(7, pesanan.getIdPelanggan());
                ps.setInt(8, pesanan.getIdMetode()); // ID metode Saldo Zalora
                ps.setInt(9, pesanan.getIdKurir());
                if (pesanan.getKodeVoucher() != null && !pesanan.getKodeVoucher().trim().isEmpty()) {
                    ps.setString(10, pesanan.getKodeVoucher());
                } else {
                    ps.setNull(10, Types.VARCHAR);
                }
                ps.executeUpdate();
            }

            // -------------------------------------------------------
            // LANGKAH 4 & 5: INSERT detail pesanan + potong stok
            // -------------------------------------------------------
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement psStok   = conn.prepareStatement(sqlUpdateStok)) {
                for (DetailPesanan det : listDetail) {
                    // INSERT detail pesanan
                    psDetail.setInt(1, pesanan.getIdPesanan());
                    psDetail.setInt(2, det.getIdVarian());
                    psDetail.setDouble(3, det.getSubtotal());
                    psDetail.setInt(4, det.getJumlahBeli());
                    psDetail.setDouble(5, det.getHargaSaatBeli());
                    psDetail.executeUpdate();

                    // Kurangi stok varian produk
                    psStok.setInt(1, det.getJumlahBeli());
                    psStok.setInt(2, det.getIdVarian());
                    psStok.executeUpdate();
                }
            }

            // -------------------------------------------------------
            // LANGKAH 6: Potong kuota voucher (jika digunakan)
            // -------------------------------------------------------
            if (pesanan.getKodeVoucher() != null && !pesanan.getKodeVoucher().trim().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateVoucher)) {
                    ps.setString(1, pesanan.getKodeVoucher());
                    ps.executeUpdate();
                }
            }

            // -------------------------------------------------------
            // LANGKAH 7: Potong saldo pelanggan
            // -------------------------------------------------------
            double saldoSesudah = saldoSebelum - totalTagihan;
            try (PreparedStatement ps = conn.prepareStatement(sqlPotongSaldo)) {
                ps.setDouble(1, totalTagihan);
                ps.setInt(2, pesanan.getIdPelanggan());
                ps.executeUpdate();
            }

            // -------------------------------------------------------
            // LANGKAH 8: INSERT riwayat saldo (PEMBAYARAN)
            // -------------------------------------------------------
            int nextIdMutasi = getNextIdMutasiSaldo(conn);
            String keterangan = "Pembayaran pesanan #" + pesanan.getIdPesanan() + " menggunakan Saldo Zalora";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertRiwayat)) {
                ps.setInt(1, nextIdMutasi);
                ps.setInt(2, pesanan.getIdPelanggan());
                ps.setDouble(3, totalTagihan);
                ps.setDouble(4, saldoSebelum);
                ps.setDouble(5, saldoSesudah);
                ps.setString(6, keterangan);
                ps.setInt(7, pesanan.getIdPesanan());
                ps.executeUpdate();
            }

            // -------------------------------------------------------
            // COMMIT: Semua langkah berhasil, sahkan ke database
            // -------------------------------------------------------
            conn.commit();
            System.out.println("Checkout berhasil! Pesanan #" + pesanan.getIdPesanan()
                    + " | Saldo berkurang Rp " + totalTagihan
                    + " | Saldo baru: Rp " + saldoSesudah);
            return true;

        } catch (SQLException e) {
            // Jika ada error di langkah mana pun, batalkan semua perubahan
            if (conn != null) {
                try {
                    System.err.println("Checkout gagal karena error SQL! Melakukan Rollback...");
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Helper: Mengambil ID mutasi saldo berikutnya dalam transaction yang sama.
     * Menggunakan Connection yang sudah terbuka agar tetap dalam satu transaction.
     */
    private int getNextIdMutasiSaldo(Connection conn) throws SQLException {
        String sql = "SELECT ISNULL(MAX(ID_Mutasi), 0) + 1 AS next_id FROM RIWAYAT_SALDO_ZALORA";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("next_id");
        }
        return 1;
    }

    // =========================================================================
    // 4. OPERASI CRUD: ULASAN BENTUK ASOSIATIF (INSERT & READ)
    // =========================================================================

    // CREATE: Menulis ulasan baru untuk produk yang sudah selesai dibeli
    public boolean insertUlasan(model.Ulasan ulasan) {
        String sql = "INSERT INTO ULASAN (ID_Ulasan, ID_Pelanggan, SKU_Produk, Tanggal_Ulasan, Rating) VALUES (?, ?, ?, GETDATE(), ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, ulasan.getIdUlasan());
            ps.setInt(2, ulasan.getIdPelanggan());
            ps.setString(3, ulasan.getSkuProduk());
            ps.setInt(4, ulasan.getRating());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // 5. KERI ANALISIS DML LANJUT (MEMENUHI POIN TUGAS: AGREGASI & GROUP BY)
    // =========================================================================

    // ANALISIS 1 (Lama): Top 3 produk terlaris - hanya output ke console
    public void cetakProdukTerlaris() {
        String sql = "SELECT TOP 3 p.Nama_Produk, SUM(dp.Jumlah_Beli) AS Total_Terjual "
                   + "FROM DETAIL_PESANAN dp "
                   + "JOIN VARIAN_PRODUK vp ON dp.ID_Varian = vp.ID_Varian "
                   + "JOIN PRODUK p ON vp.SKU_Produk = p.SKU_Produk "
                   + "GROUP BY p.Nama_Produk "
                   + "ORDER BY Total_Terjual DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("=== TOP 3 PRODUK TERLARIS ZALORA ===");
            while (rs.next()) {
                System.out.println("- " + rs.getString("Nama_Produk")
                        + " (Terjual: " + rs.getInt("Total_Terjual") + " pcs)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * ANALISIS BARU: Mengambil TOP 5 produk terlaris untuk ditampilkan di panel admin.
     *
     * Menggunakan:
     * - TOP 5       : batasi hasil hanya 5 baris teratas
     * - JOIN        : menghubungkan DETAIL_PESANAN, VARIAN_PRODUK, PRODUK, PESANAN
     * - GROUP BY    : mengelompokkan per produk
     * - SUM         : menjumlahkan total unit terjual dan total pendapatan
     * - ORDER BY    : diurutkan dari yang paling banyak terjual
     * - WHERE       : mengecualikan pesanan yang dibatalkan
     *
     * Setiap baris berisi:
     * [0] Ranking (int, dihitung dari urutan)
     * [1] SKU_Produk (String)
     * [2] Nama_Produk (String)
     * [3] Total_Terjual (int)
     * [4] Total_Pendapatan (double)
     *
     * @return list TOP 5 produk terlaris
     */
    public List<Object[]> getTop5ProdukTerlaris() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TOP 5 "
                   + "    p.SKU_Produk, "
                   + "    p.Nama_Produk, "
                   + "    SUM(dp.Jumlah_Beli) AS Total_Terjual, "
                   + "    SUM(dp.Subtotal)    AS Total_Pendapatan "
                   + "FROM DETAIL_PESANAN dp "
                   + "JOIN VARIAN_PRODUK vp ON dp.ID_Varian = vp.ID_Varian "
                   + "JOIN PRODUK p         ON vp.SKU_Produk = p.SKU_Produk "
                   + "JOIN PESANAN ps       ON dp.ID_Pesanan = ps.ID_Pesanan "
                   + "WHERE ps.Status_Pesanan <> 'Dibatalkan' "
                   + "GROUP BY p.SKU_Produk, p.Nama_Produk "
                   + "ORDER BY Total_Terjual DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int ranking = 1;
            while (rs.next()) {
                list.add(new Object[]{
                    ranking++,
                    rs.getString("SKU_Produk"),
                    rs.getString("Nama_Produk"),
                    rs.getInt("Total_Terjual"),
                    rs.getDouble("Total_Pendapatan")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ANALISIS 2: Total pendapatan dari pesanan Selesai
    public double getTotalPendapatanSelesai() {
        String sql = "SELECT SUM(Total_Tagihan) AS Total_Omset FROM PESANAN WHERE Status_Pesanan = 'Selesai'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("Total_Omset");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // ANALISIS 3: Statistik penggunaan voucher
    public void cetakStatistikPenggunaanVoucher() {
        String sql = "SELECT Kode_Voucher, COUNT(ID_Pesanan) AS Jumlah_Penggunaan "
                   + "FROM PESANAN WHERE Kode_Voucher IS NOT NULL GROUP BY Kode_Voucher";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("=== STATISTIK PENGGUNAAN VOUCHER ===");
            while (rs.next()) {
                System.out.println("Voucher: " + rs.getString("Kode_Voucher")
                        + " -> Dipakai: " + rs.getInt("Jumlah_Penggunaan") + " kali");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} // AKHIR DARI FILE TransactionDAO.java
