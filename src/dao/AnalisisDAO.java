package dao;

import config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnalisisDAO {



    public List<Object[]> getTop5ProdukTerlaris() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT SKU_Produk, Nama_Produk, Merek, Nama_Kategori, " +
                     "Total_Terjual, Total_Pendapatan, Jumlah_Transaksi " +
                     "FROM VW_TOP5_PRODUK_TERLARIS";
        try (Connection conn = DBConnection.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {

            int ranking = 1;
            while (rs.next()) {
                list.add(new Object[]{
                    ranking++,
                    rs.getString("SKU_Produk"),
                    rs.getString("Nama_Produk"),
                    rs.getString("Merek"),
                    rs.getString("Nama_Kategori"),
                    rs.getInt("Total_Terjual"),
                    rs.getDouble("Total_Pendapatan"),
                    rs.getInt("Jumlah_Transaksi")
                });
            }
        } catch (SQLException e) {
            System.err.println("[AnalisisDAO] Error getTop5ProdukTerlaris: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }



    public List<Object[]> getTop5PelangganTerbelajaViaStoredProc(Integer bulanFilter, Integer tahunFilter) {
        List<Object[]> list = new ArrayList<>();
        String sql = "{CALL SP_TOP5_PELANGGAN_TERBANYAK_BELANJA(?, ?)}";
        try (Connection       conn = DBConnection.getConnection();
             CallableStatement cs   = conn.prepareCall(sql)) {

                        if (bulanFilter != null) {
                cs.setInt(1, bulanFilter);
            } else {
                cs.setNull(1, Types.INTEGER);
            }
            if (tahunFilter != null) {
                cs.setInt(2, tahunFilter);
            } else {
                cs.setNull(2, Types.INTEGER);
            }

            try (ResultSet rs = cs.executeQuery()) {
                int ranking = 1;
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("Pesanan_Terakhir");
                    list.add(new Object[]{
                        ranking++,
                        rs.getInt("ID_Pelanggan"),
                        rs.getString("Nama_Akun"),
                        rs.getString("Surel"),
                        rs.getInt("Jumlah_Pesanan"),
                        rs.getDouble("Total_Belanja"),
                        rs.getDouble("Rata_Rata_Belanja"),
                        ts != null ? ts.toString().substring(0, 16) : "-"
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnalisisDAO] Error getTop5Pelanggan SP: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }


    public List<Integer> getTahunTersedia() {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(Waktu_Transaksi) AS Tahun " +
                     "FROM PESANAN ORDER BY Tahun DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(rs.getInt("Tahun"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }



    public List<Object[]> getAllProdukUntukDropdown() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT SKU_Produk, Nama_Produk FROM PRODUK ORDER BY Nama_Produk";
        try (Connection conn = DBConnection.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("SKU_Produk"),
                    rs.getString("Nama_Produk")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public List<Object[]> get3ProdukSeringDibeliBareng(String skuProdukAcuan) {
        List<Object[]> list = new ArrayList<>();

        String sql =
            "SELECT TOP 3 " +
            "    p2.SKU_Produk          AS SKU_Pasangan, " +
            "    p2.Nama_Produk         AS Nama_Pasangan, " +
            "    p2.Merek               AS Merek_Pasangan, " +
            "    COUNT(*)               AS Frekuensi_Bersamaan " +
            "FROM DETAIL_PESANAN dp1 " +
                        "JOIN DETAIL_PESANAN dp2 " +
            "    ON  dp1.ID_Pesanan = dp2.ID_Pesanan " +
            "    AND dp1.ID_Varian  <> dp2.ID_Varian " +
            "JOIN VARIAN_PRODUK vp1 ON dp1.ID_Varian = vp1.ID_Varian " +
            "JOIN VARIAN_PRODUK vp2 ON dp2.ID_Varian = vp2.ID_Varian " +
            "JOIN PRODUK p1 ON vp1.SKU_Produk = p1.SKU_Produk " +
            "JOIN PRODUK p2 ON vp2.SKU_Produk = p2.SKU_Produk " +
            "JOIN PESANAN ps ON dp1.ID_Pesanan = ps.ID_Pesanan " +
            "WHERE p1.SKU_Produk = ? " +
            "  AND p1.SKU_Produk <> p2.SKU_Produk " +
            "  AND ps.Status_Pesanan <> 'Dibatalkan' " +
            "GROUP BY p2.SKU_Produk, p2.Nama_Produk, p2.Merek " +
            "ORDER BY Frekuensi_Bersamaan DESC";

        try (Connection        conn = DBConnection.getConnection();
             PreparedStatement ps   = conn.prepareStatement(sql)) {

            ps.setString(1, skuProdukAcuan);

            try (ResultSet rs = ps.executeQuery()) {
                int ranking = 1;
                while (rs.next()) {
                    list.add(new Object[]{
                        ranking++,
                        rs.getString("SKU_Pasangan"),
                        rs.getString("Nama_Pasangan"),
                        rs.getString("Merek_Pasangan"),
                        rs.getInt("Frekuensi_Bersamaan")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnalisisDAO] Error get3ProdukBareng: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}
