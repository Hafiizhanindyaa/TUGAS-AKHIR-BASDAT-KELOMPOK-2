package dao;

import config.DBConnection;
import model.Voucher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromoDAO {

    public List<Voucher> getAllVoucher() {
        List<Voucher> list = new ArrayList<>();
        String sql = "SELECT * FROM VOUCHER ORDER BY Tanggal_Berakhir DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Voucher(
                    rs.getString("Kode_Voucher"),
                    rs.getString("Tipe_Diskon"),
                    rs.getDouble("Nilai_Diskon"),
                    rs.getDouble("Minimum_Belanja"),
                    rs.getDate("Tanggal_Mulai"),
                    rs.getDate("Tanggal_Berakhir"),
                    rs.getInt("Kuota_Pemakaian")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insertVoucher(Voucher v) {
        String sql = "INSERT INTO VOUCHER (Kode_Voucher, Tipe_Diskon, Nilai_Diskon, Minimum_Belanja, Tanggal_Mulai, Tanggal_Berakhir, Kuota_Pemakaian) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getKodeVoucher());
            ps.setString(2, v.getTipeDiskon());
            ps.setDouble(3, v.getNilaiDiskon());
            ps.setDouble(4, v.getMinimumBelanja());
            ps.setDate(5, new java.sql.Date(v.getTanggalMulai().getTime()));
            ps.setDate(6, new java.sql.Date(v.getTanggalBerakhir().getTime()));
            ps.setInt(7, v.getKuotaPemakaian());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateVoucher(Voucher v) {
        String sql = "UPDATE VOUCHER SET Tipe_Diskon=?, Nilai_Diskon=?, Minimum_Belanja=?, Tanggal_Mulai=?, Tanggal_Berakhir=?, Kuota_Pemakaian=? WHERE Kode_Voucher=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getTipeDiskon());
            ps.setDouble(2, v.getNilaiDiskon());
            ps.setDouble(3, v.getMinimumBelanja());
            ps.setDate(4, new java.sql.Date(v.getTanggalMulai().getTime()));
            ps.setDate(5, new java.sql.Date(v.getTanggalBerakhir().getTime()));
            ps.setInt(6, v.getKuotaPemakaian());
            ps.setString(7, v.getKodeVoucher());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteVoucher(String kodeVoucher) {
        String sql = "DELETE FROM VOUCHER WHERE Kode_Voucher=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodeVoucher);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
