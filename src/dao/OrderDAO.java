package dao;

import config.DBConnection;
import model.Pesanan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // READ: Semua pesanan dengan info pelanggan (JOIN) untuk admin
    public List<Object[]> getAllPesananAdmin() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT ps.ID_Pesanan, pl.Nama_Akun, ps.Waktu_Transaksi, "
                   + "ps.Total_Tagihan, ps.Status_Pesanan, ps.Kode_Voucher, "
                   + "k.Nama_Ekspedisi, k.Jenis_Layanan "
                   + "FROM PESANAN ps "
                   + "JOIN PELANGGAN pl ON ps.ID_Pelanggan = pl.ID_Pelanggan "
                   + "JOIN KURIR k ON ps.ID_Kurir = k.ID_Kurir "
                   + "ORDER BY ps.Waktu_Transaksi DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("ID_Pesanan"),
                    rs.getString("Nama_Akun"),
                    rs.getTimestamp("Waktu_Transaksi"),
                    rs.getDouble("Total_Tagihan"),
                    rs.getString("Status_Pesanan"),
                    rs.getString("Kode_Voucher") != null ? rs.getString("Kode_Voucher") : "-",
                    rs.getString("Nama_Ekspedisi") + " - " + rs.getString("Jenis_Layanan")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // READ: Pesanan milik pelanggan tertentu
    public List<Object[]> getPesananByPelanggan(int idPelanggan) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT ps.ID_Pesanan, ps.Waktu_Transaksi, ps.Total_Tagihan, "
                   + "ps.Status_Pesanan, k.Nama_Ekspedisi "
                   + "FROM PESANAN ps "
                   + "JOIN KURIR k ON ps.ID_Kurir = k.ID_Kurir "
                   + "WHERE ps.ID_Pelanggan = ? "
                   + "ORDER BY ps.Waktu_Transaksi DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("ID_Pesanan"),
                        rs.getTimestamp("Waktu_Transaksi"),
                        rs.getDouble("Total_Tagihan"),
                        rs.getString("Status_Pesanan"),
                        rs.getString("Nama_Ekspedisi")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // READ: Detail item dalam 1 pesanan
    public List<Object[]> getDetailPesanan(int idPesanan) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT p.Nama_Produk, vp.Warna, vp.Ukuran, dp.Jumlah_Beli, dp.Harga_Saat_Beli, dp.Subtotal "
                   + "FROM DETAIL_PESANAN dp "
                   + "JOIN VARIAN_PRODUK vp ON dp.ID_Varian = vp.ID_Varian "
                   + "JOIN PRODUK p ON vp.SKU_Produk = p.SKU_Produk "
                   + "WHERE dp.ID_Pesanan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPesanan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("Nama_Produk"),
                        rs.getString("Warna"),
                        rs.getString("Ukuran"),
                        rs.getInt("Jumlah_Beli"),
                        rs.getDouble("Harga_Saat_Beli"),
                        rs.getDouble("Subtotal")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // UPDATE: Admin update status pesanan
    public boolean updateStatusPesanan(int idPesanan, String statusBaru) {
        String sql = "UPDATE PESANAN SET Status_Pesanan=? WHERE ID_Pesanan=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statusBaru);
            ps.setInt(2, idPesanan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // READ: Kurir untuk combo checkout
    public List<Object[]> getAllKurirForCombo() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT ID_Kurir, Nama_Ekspedisi, Jenis_Layanan FROM KURIR ORDER BY ID_Kurir";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("ID_Kurir"),
                    rs.getString("Nama_Ekspedisi") + " - " + rs.getString("Jenis_Layanan")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Method lama getAllMetodeForCombo() dipertahankan agar tidak merusak kode lain.
     * Namun penggunaannya pada checkout sudah DIPINDAH ke getMetodePembayaranUntukTopUp().
     * Method ini tidak lagi digunakan oleh CheckoutPanel.
     */
    public List<Object[]> getAllMetodeForCombo() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT ID_Metode, Nama_Metode, Tipe_Metode "
                   + "FROM METODE_PEMBAYARAN ORDER BY ID_Metode";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("ID_Metode"),
                    rs.getString("Nama_Metode") + " (" + rs.getString("Tipe_Metode") + ")"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Mengambil metode pembayaran khusus untuk TOP UP Saldo Zalora.
     *
     * Mengambil semua metode pembayaran KECUALI 'Saldo Zalora',
     * karena saldo tidak boleh dipakai untuk top up saldo itu sendiri.
     *
     * Digunakan oleh: TopUpSaldoPanel.java
     * TIDAK digunakan oleh: CheckoutPanel.java
     *
     * Setiap baris berisi:
     * [0] ID_Metode (int)
     * [1] Label untuk combo: "Nama (Tipe)"
     * [2] Nama_Metode (String)
     * [3] Tipe_Metode (String)
     * [4] Nama_Provider (String)
     *
     * @return list metode pembayaran untuk top up
     */
    public List<Object[]> getMetodePembayaranUntukTopUp() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT ID_Metode, Nama_Metode, Tipe_Metode, Nama_Provider "
                   + "FROM METODE_PEMBAYARAN "
                   + "WHERE Nama_Metode <> 'Saldo Zalora' "
                   + "ORDER BY Nama_Metode";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String namaMetode  = rs.getString("Nama_Metode");
                String tipeMetode  = rs.getString("Tipe_Metode");
                String namaProvider = rs.getString("Nama_Provider");
                String labelCombo  = namaMetode + " (" + tipeMetode + ")";
                list.add(new Object[]{
                    rs.getInt("ID_Metode"),
                    labelCombo,
                    namaMetode,
                    tipeMetode,
                    namaProvider != null ? namaProvider : "-"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Mengambil ID metode pembayaran 'Saldo Zalora' dari database.
     * Digunakan oleh CheckoutPanel saat membuat pesanan baru,
     * agar kolom ID_Metode pada tabel PESANAN tetap terisi dengan benar.
     *
     * @return ID_Metode dari baris 'Saldo Zalora', atau -1 jika tidak ditemukan
     */
    public int getIdMetodeSaldoZalora() {
        String sql = "SELECT ID_Metode FROM METODE_PEMBAYARAN WHERE Nama_Metode = 'Saldo Zalora'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("ID_Metode");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.err.println("PERINGATAN: Metode 'Saldo Zalora' tidak ditemukan di database!");
        System.err.println("Jalankan SQL_TAMBAHAN_JALANKAN_DULU.sql terlebih dahulu.");
        return -1;
    }

    // READ: Alamat untuk combo checkout
    public List<Object[]> getAlamatForCombo(int idPelanggan) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT Label_Alamat, Kota FROM ALAMAT_KIRIM WHERE ID_Pelanggan=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("Label_Alamat"),
                        rs.getString("Label_Alamat") + " - " + rs.getString("Kota")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // READ: Next ID Pesanan (auto-generate)
    public int getNextIdPesanan() {
        String sql = "SELECT ISNULL(MAX(ID_Pesanan), 0) + 1 AS next_id FROM PESANAN";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("next_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return 1;
    }

    // READ: Ambil data pesanan berdasarkan ID (untuk keperluan refund)
    public Pesanan getPesananById(int idPesanan) {
        String sql = "SELECT * FROM PESANAN WHERE ID_Pesanan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPesanan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Pesanan p = new Pesanan();
                    p.setIdPesanan(rs.getInt("ID_Pesanan"));
                    p.setTotalHargaBarang(rs.getDouble("Total_Harga_Barang"));
                    p.setBiayaPengiriman(rs.getDouble("Biaya_Pengiriman"));
                    p.setTotalTagihan(rs.getDouble("Total_Tagihan"));
                    p.setStatusPesanan(rs.getString("Status_Pesanan"));
                    p.setLabelAlamat(rs.getString("Label_Alamat"));
                    p.setIdPelanggan(rs.getInt("ID_Pelanggan"));
                    p.setIdMetode(rs.getInt("ID_Metode"));
                    p.setIdKurir(rs.getInt("ID_Kurir"));
                    p.setKodeVoucher(rs.getString("Kode_Voucher"));
                    return p;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
