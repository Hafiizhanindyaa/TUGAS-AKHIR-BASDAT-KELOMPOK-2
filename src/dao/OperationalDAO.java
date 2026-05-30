package dao;

import config.DBConnection;
import model.Kurir;
import model.Kategori;
import model.MetodePembayaran;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OperationalDAO {


    public List<Kurir> getAllKurir() {
        List<Kurir> list = new ArrayList<>();
        String sql = "SELECT * FROM KURIR ORDER BY ID_Kurir";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Kurir(
                    rs.getInt("ID_Kurir"),
                    rs.getString("Nama_Ekspedisi"),
                    rs.getString("Jenis_Layanan"),
                    rs.getString("Estimasi_Waktu")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insertKurir(Kurir k) {
        String sql = "INSERT INTO KURIR (ID_Kurir, Nama_Ekspedisi, Jenis_Layanan, Estimasi_Waktu) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, k.getIdKurir());
            ps.setString(2, k.getNamaEkspedisi());
            ps.setString(3, k.getJenisLayanan());
            ps.setString(4, k.getEstimasiWaktu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateKurir(Kurir k) {
        String sql = "UPDATE KURIR SET Nama_Ekspedisi=?, Jenis_Layanan=?, Estimasi_Waktu=? WHERE ID_Kurir=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k.getNamaEkspedisi());
            ps.setString(2, k.getJenisLayanan());
            ps.setString(3, k.getEstimasiWaktu());
            ps.setInt(4, k.getIdKurir());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteKurir(int idKurir) {
        String sql = "DELETE FROM KURIR WHERE ID_Kurir=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKurir);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }


    public List<Kategori> getAllKategori() {
        List<Kategori> list = new ArrayList<>();
        String sql = "SELECT * FROM KATEGORI ORDER BY ID_Kategori";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Kategori(
                    rs.getInt("ID_Kategori"),
                    rs.getString("Nama_Kategori"),
                    rs.getString("Target_Gender")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insertKategori(Kategori k) {
        String sql = "INSERT INTO KATEGORI (ID_Kategori, Nama_Kategori, Target_Gender) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, k.getIdKategori());
            ps.setString(2, k.getNamaKategori());
            ps.setString(3, k.getTargetGender());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateKategori(Kategori k) {
        String sql = "UPDATE KATEGORI SET Nama_Kategori=?, Target_Gender=? WHERE ID_Kategori=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k.getNamaKategori());
            ps.setString(2, k.getTargetGender());
            ps.setInt(3, k.getIdKategori());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteKategori(int idKategori) {
        String sql = "DELETE FROM KATEGORI WHERE ID_Kategori=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKategori);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }


    public List<MetodePembayaran> getAllMetode() {
        List<MetodePembayaran> list = new ArrayList<>();
        String sql = "SELECT * FROM METODE_PEMBAYARAN ORDER BY ID_Metode";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new MetodePembayaran(
                    rs.getInt("ID_Metode"),
                    rs.getString("Nama_Metode"),
                    rs.getString("Tipe_Metode"),
                    rs.getString("Nama_Provider")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insertMetode(MetodePembayaran m) {
        String sql = "INSERT INTO METODE_PEMBAYARAN (ID_Metode, Nama_Metode, Tipe_Metode, Nama_Provider) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getIdMetode());
            ps.setString(2, m.getNamaMetode());
            ps.setString(3, m.getTipeMetode());
            ps.setString(4, m.getNamaProvider());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateMetode(MetodePembayaran m) {
        String sql = "UPDATE METODE_PEMBAYARAN SET Nama_Metode=?, Tipe_Metode=?, Nama_Provider=? WHERE ID_Metode=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getNamaMetode());
            ps.setString(2, m.getTipeMetode());
            ps.setString(3, m.getNamaProvider());
            ps.setInt(4, m.getIdMetode());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteMetode(int idMetode) {
        String sql = "DELETE FROM METODE_PEMBAYARAN WHERE ID_Metode=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMetode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
