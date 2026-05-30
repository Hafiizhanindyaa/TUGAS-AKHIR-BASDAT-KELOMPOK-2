package dao;

import config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaldoDAO {


    public double getSaldoPelanggan(int idPelanggan) {
        String sql = "SELECT Saldo_Zalora FROM PELANGGAN WHERE ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("Saldo_Zalora");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean isSaldoCukup(int idPelanggan, double totalTagihan) {
        double saldo = getSaldoPelanggan(idPelanggan);
        return saldo >= totalTagihan;
    }



    public int getNextIdMutasiSaldo() {
        String sql = "SELECT ISNULL(MAX(ID_Mutasi), 0) + 1 AS next_id FROM RIWAYAT_SALDO_ZALORA";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("next_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }


    private int getNextIdMutasiSaldo(Connection conn) throws SQLException {
        String sql = "SELECT ISNULL(MAX(ID_Mutasi), 0) + 1 AS next_id FROM RIWAYAT_SALDO_ZALORA";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("next_id");
        }
        return 1;
    }


    public boolean topUpSaldo(int idPelanggan, double nominal, int idMetodePembayaran, String namaMetode) {
        String sqlAmbilSaldo    = "SELECT Saldo_Zalora FROM PELANGGAN WHERE ID_Pelanggan = ?";
        String sqlUpdateSaldo   = "UPDATE PELANGGAN SET Saldo_Zalora = Saldo_Zalora + ? WHERE ID_Pelanggan = ?";
        String sqlInsertRiwayat = "INSERT INTO RIWAYAT_SALDO_ZALORA "
        + "(ID_Mutasi, ID_Pelanggan, Jenis_Mutasi, Nominal, "
                + " Saldo_Sebelum, Saldo_Sesudah, Waktu_Mutasi, Keterangan, ID_Pesanan, ID_Metode) "
                + "VALUES (?, ?, 'TOPUP', ?, ?, ?, GETDATE(), ?, NULL, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

                        double saldoSebelum = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlAmbilSaldo)) {
                ps.setInt(1, idPelanggan);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) saldoSebelum = rs.getDouble("Saldo_Zalora");
                }
            }
            double saldoSesudah = saldoSebelum + nominal;

                        try (PreparedStatement ps = conn.prepareStatement(sqlUpdateSaldo)) {
                ps.setDouble(1, nominal);
                ps.setInt(2, idPelanggan);
                ps.executeUpdate();
            }

                        int nextId = getNextIdMutasiSaldo(conn);
            String keterangan = "Top up Saldo Zalora menggunakan " + namaMetode;
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertRiwayat)) {
                ps.setInt(1, nextId);
                ps.setInt(2, idPelanggan);
                ps.setDouble(3, nominal);
                ps.setDouble(4, saldoSebelum);
                ps.setDouble(5, saldoSesudah);
                ps.setString(6, keterangan);
                ps.setInt(7, idMetodePembayaran);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Top up berhasil: Rp " + nominal + " | Saldo baru: Rp " + saldoSesudah);
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.err.println("Top up gagal! Melakukan Rollback...");
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public List<Object[]> getRiwayatSaldo(int idPelanggan) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT r.ID_Mutasi, r.Jenis_Mutasi, r.Nominal, r.Saldo_Sebelum, "
                   + "       r.Saldo_Sesudah, r.Waktu_Mutasi, r.Keterangan, r.ID_Pesanan, "
                   + "       m.Nama_Metode "
                   + "FROM RIWAYAT_SALDO_ZALORA r "
                   + "LEFT JOIN METODE_PEMBAYARAN m ON r.ID_Metode = m.ID_Metode "
                   + "WHERE r.ID_Pelanggan = ? "
                   + "ORDER BY r.Waktu_Mutasi DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer idPesanan = rs.getObject("ID_Pesanan") != null
                                      ? rs.getInt("ID_Pesanan") : null;
                    String namaMetode = rs.getString("Nama_Metode");
                    list.add(new Object[]{
                        rs.getInt("ID_Mutasi"),
                        rs.getString("Jenis_Mutasi"),
                        rs.getDouble("Nominal"),
                        rs.getDouble("Saldo_Sebelum"),
                        rs.getDouble("Saldo_Sesudah"),
                        rs.getTimestamp("Waktu_Mutasi"),
                        rs.getString("Keterangan"),
                        idPesanan,
                        namaMetode != null ? namaMetode : "-"
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public boolean isSudahPernahRefund(int idPesanan) {
        String sql = "SELECT COUNT(*) FROM RIWAYAT_SALDO_ZALORA "
                   + "WHERE ID_Pesanan = ? AND Jenis_Mutasi = 'REFUND'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPesanan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean refundSaldo(int idPesanan) {
        String sqlAmbilPesanan  = "SELECT ID_Pelanggan, Total_Tagihan FROM PESANAN WHERE ID_Pesanan = ?";
        String sqlAmbilSaldo    = "SELECT Saldo_Zalora FROM PELANGGAN WHERE ID_Pelanggan = ?";
        String sqlUpdateSaldo   = "UPDATE PELANGGAN SET Saldo_Zalora = Saldo_Zalora + ? WHERE ID_Pelanggan = ?";
        String sqlInsertRiwayat = "INSERT INTO RIWAYAT_SALDO_ZALORA "
        + "(ID_Mutasi, ID_Pelanggan, Jenis_Mutasi, Nominal, "
                + " Saldo_Sebelum, Saldo_Sesudah, Waktu_Mutasi, Keterangan, ID_Pesanan, ID_Metode) "
                + "VALUES (?, ?, 'REFUND', ?, ?, ?, GETDATE(), ?, ?, NULL)";

        if (isSudahPernahRefund(idPesanan)) {
            System.out.println("Pesanan #" + idPesanan + " sudah pernah direfund sebelumnya.");
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int idPelanggan = 0;
            double nominal  = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlAmbilPesanan)) {
                ps.setInt(1, idPesanan);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idPelanggan = rs.getInt("ID_Pelanggan");
                        nominal     = rs.getDouble("Total_Tagihan");
                    } else {
                        System.err.println("Refund gagal: Pesanan #" + idPesanan + " tidak ditemukan.");
                        conn.rollback();
                        return false;
                    }
                }
            }

            double saldoSebelum = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlAmbilSaldo)) {
                ps.setInt(1, idPelanggan);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) saldoSebelum = rs.getDouble("Saldo_Zalora");
                }
            }
            double saldoSesudah = saldoSebelum + nominal;

            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateSaldo)) {
                ps.setDouble(1, nominal);
                ps.setInt(2, idPelanggan);
                ps.executeUpdate();
            }

                        int nextId = getNextIdMutasiSaldo(conn);
            String keterangan = "Refund pesanan dibatalkan #" + idPesanan;
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertRiwayat)) {
                ps.setInt(1, nextId);
                ps.setInt(2, idPelanggan);
                ps.setDouble(3, nominal);
                ps.setDouble(4, saldoSebelum);
                ps.setDouble(5, saldoSesudah);
                ps.setString(6, keterangan);
                ps.setInt(7, idPesanan);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Refund berhasil untuk pesanan #" + idPesanan + ": Rp " + nominal);
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.err.println("Refund gagal! Melakukan Rollback...");
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
