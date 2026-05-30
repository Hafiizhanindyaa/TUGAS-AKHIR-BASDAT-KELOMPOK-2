package dao;

import config.DBConnection;
import model.Ulasan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UlasanDAO {

        public boolean insertUlasan(Ulasan ulasan) {
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

        public List<Ulasan> getUlasanByPelanggan(int idPelanggan) {
        List<Ulasan> list = new ArrayList<>();
        String sql = "SELECT U.ID_Ulasan, U.ID_Pelanggan, U.SKU_Produk, U.Tanggal_Ulasan, U.Rating " +
                     "FROM ULASAN U WHERE U.ID_Pelanggan = ? ORDER BY U.Tanggal_Ulasan DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Ulasan(
                        rs.getInt("ID_Ulasan"),
                        rs.getInt("ID_Pelanggan"),
                        rs.getString("SKU_Produk"),
                        rs.getDate("Tanggal_Ulasan"),
                        rs.getInt("Rating")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

        public List<Object[]> getUlasanLengkapByPelanggan(int idPelanggan) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT U.ID_Ulasan, P.Nama_Produk, U.SKU_Produk, U.Rating, U.Tanggal_Ulasan " +
                     "FROM ULASAN U JOIN PRODUK P ON U.SKU_Produk = P.SKU_Produk " +
                     "WHERE U.ID_Pelanggan = ? ORDER BY U.Tanggal_Ulasan DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("ID_Ulasan"),
                        rs.getString("Nama_Produk"),
                        rs.getString("SKU_Produk"),
                        rs.getInt("Rating"),
                        rs.getDate("Tanggal_Ulasan")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

        public boolean updateUlasan(Ulasan ulasan) {
        String sql = "UPDATE ULASAN SET Rating = ?, Tanggal_Ulasan = GETDATE() WHERE ID_Ulasan = ? AND ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ulasan.getRating());
            ps.setInt(2, ulasan.getIdUlasan());
            ps.setInt(3, ulasan.getIdPelanggan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        public boolean deleteUlasan(int idUlasan, int idPelanggan) {
        String sql = "DELETE FROM ULASAN WHERE ID_Ulasan = ? AND ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUlasan);
            ps.setInt(2, idPelanggan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        public int getNextIdUlasan() {
        String sql = "SELECT ISNULL(MAX(ID_Ulasan), 0) + 1 AS NextId FROM ULASAN";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("NextId");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

        public List<Object[]> getProdukPernahDibeli(int idPelanggan) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT DISTINCT VP.SKU_Produk, P.Nama_Produk " +
                     "FROM DETAIL_PESANAN DP " +
                     "JOIN VARIAN_PRODUK VP ON DP.ID_Varian = VP.ID_Varian " +
                     "JOIN PRODUK P ON VP.SKU_Produk = P.SKU_Produk " +
                     "JOIN PESANAN PS ON DP.ID_Pesanan = PS.ID_Pesanan " +
                     "WHERE PS.ID_Pelanggan = ? AND PS.Status_Pesanan = 'Selesai'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("SKU_Produk"),
                        rs.getString("Nama_Produk")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
