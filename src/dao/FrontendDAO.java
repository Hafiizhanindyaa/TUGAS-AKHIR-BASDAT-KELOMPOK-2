package dao;

import config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FrontendDAO - DAO khusus untuk fitur frontend pelanggan.
 * Menggunakan VIEW (VW_KATALOG_PRODUK, VW_RIWAYAT_PESANAN, VW_DETAIL_PESANAN_LENGKAP)
 * dan Stored Procedure (SP_CARI_PRODUK, SP_SIMULASI_CHECKOUT).
 */
public class FrontendDAO {

    // =========================================================================
    // FITUR 1: PENCARIAN PRODUK via VIEW + SP_CARI_PRODUK
    // =========================================================================

    /**
     * Mencari produk menggunakan Stored Procedure SP_CARI_PRODUK.
     * Mendukung filter: keyword, tipe, harga min-max, kategori, sorting.
     *
     * @return List<Object[]> berisi: SKU, Nama, Merek, Harga, Kategori, Tipe, Spesifikasi, Stok, Terjual
     */
    public List<Object[]> searchProdukAdvanced(
            String keyword, String tipeProduk,
            Double hargaMin, Double hargaMax,
            String namaKategori, String sortBy, boolean sortAsc) {

        List<Object[]> list = new ArrayList<>();
        String sql = "{call SP_CARI_PRODUK(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, (keyword == null || keyword.isEmpty()) ? null : keyword);
            cs.setString(2, (tipeProduk == null || tipeProduk.equals("Semua")) ? null : tipeProduk);
            cs.setObject(3, hargaMin, Types.FLOAT);
            cs.setObject(4, hargaMax, Types.FLOAT);
            cs.setString(5, (namaKategori == null || namaKategori.equals("Semua")) ? null : namaKategori);
            cs.setString(6, sortBy == null ? "Nama_Produk" : sortBy);
            cs.setBoolean(7, sortAsc);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("SKU_Produk"),
                        rs.getString("Nama_Produk"),
                        rs.getString("Merek"),
                        rs.getDouble("Harga_Jual"),
                        rs.getString("Nama_Kategori"),
                        rs.getString("Tipe_Produk"),
                        rs.getString("Spesifikasi_Khusus"),
                        rs.getInt("Total_Stok"),
                        rs.getInt("Jumlah_Terjual")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Ambil semua kategori dari database untuk isi combo box filter.
     */
    public List<String> getAllKategori() {
        List<String> list = new ArrayList<>();
        list.add("Semua");
        String sql = "SELECT Nama_Kategori FROM KATEGORI ORDER BY Nama_Kategori";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // FITUR 2: RIWAYAT PESANAN via VIEW VW_RIWAYAT_PESANAN
    // =========================================================================

    /**
     * Ambil riwayat pesanan pelanggan dari VIEW VW_RIWAYAT_PESANAN.
     * Bisa difilter berdasarkan tanggal, bulan, tahun, dan status.
     */
    public List<Object[]> getRiwayatPesanan(int idPelanggan,
            Integer filterTahun, Integer filterBulan, String filterStatus) {

        List<Object[]> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ID_Pesanan, Waktu_Transaksi, Total_Tagihan, Status_Pesanan, ")
          .append("Info_Kurir, Kode_Voucher, Jumlah_Item, Metode_Pembayaran ")
          .append("FROM VW_RIWAYAT_PESANAN ")
          .append("WHERE ID_Pelanggan = ? ");

        if (filterTahun != null)  sb.append("AND YEAR(Waktu_Transaksi)  = ? ");
        if (filterBulan != null)  sb.append("AND MONTH(Waktu_Transaksi) = ? ");
        if (filterStatus != null && !filterStatus.equals("Semua"))
                                  sb.append("AND Status_Pesanan = ? ");
        sb.append("ORDER BY Waktu_Transaksi DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {

            int idx = 1;
            ps.setInt(idx++, idPelanggan);
            if (filterTahun != null) ps.setInt(idx++, filterTahun);
            if (filterBulan != null) ps.setInt(idx++, filterBulan);
            if (filterStatus != null && !filterStatus.equals("Semua"))
                ps.setString(idx++, filterStatus);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("ID_Pesanan"),
                        rs.getTimestamp("Waktu_Transaksi"),
                        rs.getDouble("Total_Tagihan"),
                        rs.getString("Status_Pesanan"),
                        rs.getString("Info_Kurir"),
                        rs.getString("Kode_Voucher") != null ? rs.getString("Kode_Voucher") : "-",
                        rs.getInt("Jumlah_Item"),
                        rs.getString("Metode_Pembayaran")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Ambil detail item pesanan dari VIEW VW_DETAIL_PESANAN_LENGKAP.
     */
    public List<Object[]> getDetailPesananLengkap(int idPesanan) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT Nama_Produk, Merek, Warna, Ukuran, Tipe_Produk, "
                   + "Jumlah_Beli, Harga_Saat_Beli, Subtotal "
                   + "FROM VW_DETAIL_PESANAN_LENGKAP "
                   + "WHERE ID_Pesanan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPesanan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("Nama_Produk"),
                        rs.getString("Merek"),
                        rs.getString("Warna"),
                        rs.getString("Ukuran"),
                        rs.getString("Tipe_Produk"),
                        rs.getInt("Jumlah_Beli"),
                        rs.getDouble("Harga_Saat_Beli"),
                        rs.getDouble("Subtotal")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // FITUR 3: CHECKOUT via SP_SIMULASI_CHECKOUT
    // =========================================================================

    /**
     * Memproses checkout menggunakan Stored Procedure SP_SIMULASI_CHECKOUT.
     * SP mengurus seluruh transaction: insert pesanan, detail, update stok,
     * kurangi saldo, insert riwayat saldo — semua dalam satu atomic transaction.
     *
     * @param detailItems List<Object[]>: {idVarian, namaProduk, warna, ukuran, harga, qty, subtotal}
     * @return true jika berhasil, false jika gagal
     */
    public boolean prosesCheckoutViaSP(
            int idPesanan, int idPelanggan, int idKurir, int idMetode,
            String labelAlamat, double totalHargaBarang, double biayaPengiriman,
            double totalTagihan, String kodeVoucher, List<Object[]> detailItems) {

        // Build string detail: "idVarian:qty:harga:subtotal|..."
        StringBuilder detailXML = new StringBuilder();
        for (Object[] item : detailItems) {
            int idVarian    = (int)    item[0];
            int qty         = (int)    item[5];
            double harga    = (double) item[4];
            double subtotal = (double) item[6];
            detailXML.append(idVarian).append(":")
                     .append(qty).append(":")
                     .append(harga).append(":")
                     .append(subtotal).append("|");
        }

        String sql = "{call SP_SIMULASI_CHECKOUT(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, idPesanan);
            cs.setInt(2, idPelanggan);
            cs.setInt(3, idKurir);
            cs.setInt(4, idMetode);
            cs.setString(5, labelAlamat);
            cs.setDouble(6, totalHargaBarang);
            cs.setDouble(7, biayaPengiriman);
            cs.setDouble(8, totalTagihan);
            cs.setString(9, (kodeVoucher == null || kodeVoucher.isEmpty()) ? null : kodeVoucher);
            cs.setString(10, detailXML.toString());

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("Status");
                    if ("GAGAL".equals(status)) {
                        System.err.println("SP_SIMULASI_CHECKOUT gagal: " + rs.getString("Pesan_Error"));
                        return false;
                    }
                    return "BERHASIL".equals(status);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
