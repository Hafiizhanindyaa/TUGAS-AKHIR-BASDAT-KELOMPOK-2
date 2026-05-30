package dao;

import config.DBConnection;
import model.Pelanggan;
import model.PelangganNomorTeleponAktif;
import model.AlamatKirim;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {


        public boolean insertPelanggan(Pelanggan pelanggan) {
        String sql = "INSERT INTO PELANGGAN (ID_Pelanggan, Nama_Akun, Surel, Kata_Sandi, Tanggal_Lahir) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pelanggan.getIdPelanggan());
            ps.setString(2, pelanggan.getNamaAkun());
            ps.setString(3, pelanggan.getSurel());
            ps.setString(4, pelanggan.getKataSandi());
            ps.setDate(5, new java.sql.Date(pelanggan.getTanggalLahir().getTime()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        public List<Pelanggan> getAllPelanggan() {
        List<Pelanggan> list = new ArrayList<>();
        String sql = "SELECT * FROM PELANGGAN";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Pelanggan p = new Pelanggan(
                    rs.getInt("ID_Pelanggan"),
                    rs.getString("Nama_Akun"),
                    rs.getString("Surel"),
                    rs.getString("Kata_Sandi"),
                    rs.getDate("Tanggal_Lahir")
                );
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

        public boolean updatePelanggan(Pelanggan pelanggan) {
        String sql = "UPDATE PELANGGAN SET Nama_Akun = ?, Surel = ?, Kata_Sandi = ?, Tanggal_Lahir = ? WHERE ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pelanggan.getNamaAkun());
            ps.setString(2, pelanggan.getSurel());
            ps.setString(3, pelanggan.getKataSandi());
            ps.setDate(4, new java.sql.Date(pelanggan.getTanggalLahir().getTime()));
            ps.setInt(5, pelanggan.getIdPelanggan());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        public boolean deletePelanggan(int idPelanggan) {
        String sql = "DELETE FROM PELANGGAN WHERE ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPelanggan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public int getNextIdPelanggan() {
        String sql = "SELECT ISNULL(MAX(ID_Pelanggan), 0) + 1 AS next_id FROM PELANGGAN";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("next_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }


    public boolean isNamaAkunExists(String namaAkun) {
        String sql = "SELECT COUNT(*) FROM PELANGGAN WHERE Nama_Akun = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, namaAkun);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean isSurelExists(String surel) {
        String sql = "SELECT COUNT(*) FROM PELANGGAN WHERE Surel = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, surel);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean isNomorTeleponExists(String nomorTelepon) {
        String sql = "SELECT COUNT(*) FROM PELANGGAN_NOMOR_TELEPON_AKTIF WHERE Nomor_Telepon_Aktif = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomorTelepon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean registerPelangganDenganTelepon(Pelanggan pelanggan, String nomorTelepon) {
        String sqlPelanggan = "INSERT INTO PELANGGAN "
        + "(ID_Pelanggan, Nama_Akun, Surel, Kata_Sandi, Tanggal_Lahir) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlNoTelp = "INSERT INTO PELANGGAN_NOMOR_TELEPON_AKTIF "
        + "(Nomor_Telepon_Aktif, ID_Pelanggan) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

                        try (PreparedStatement ps1 = conn.prepareStatement(sqlPelanggan)) {
                ps1.setInt(1, pelanggan.getIdPelanggan());
                ps1.setString(2, pelanggan.getNamaAkun());
                ps1.setString(3, pelanggan.getSurel());
                ps1.setString(4, pelanggan.getKataSandi());
                ps1.setDate(5, new java.sql.Date(pelanggan.getTanggalLahir().getTime()));
                ps1.executeUpdate();
            }

                        if (nomorTelepon != null && !nomorTelepon.trim().isEmpty()) {
                try (PreparedStatement ps2 = conn.prepareStatement(sqlNoTelp)) {
                    ps2.setString(1, nomorTelepon.trim());
                    ps2.setInt(2, pelanggan.getIdPelanggan());
                    ps2.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.err.println("Registrasi gagal! Melakukan Rollback...");
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


        public boolean insertNoTelp(PelangganNomorTeleponAktif noTelp) {
        String sql = "INSERT INTO PELANGGAN_NOMOR_TELEPON_AKTIF (Nomor_Telepon_Aktif, ID_Pelanggan) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, noTelp.getNomorTeleponAktif());
            ps.setInt(2, noTelp.getIdPelanggan());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        public List<PelangganNomorTeleponAktif> getNoTelpByPelanggan(int idPelanggan) {
        List<PelangganNomorTeleponAktif> list = new ArrayList<>();
        String sql = "SELECT * FROM PELANGGAN_NOMOR_TELEPON_AKTIF WHERE ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PelangganNomorTeleponAktif nt = new PelangganNomorTeleponAktif(
                        rs.getString("Nomor_Telepon_Aktif"),
                        rs.getInt("ID_Pelanggan")
                    );
                    list.add(nt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

        public boolean deleteNoTelp(String noTelp, int idPelanggan) {
        String sql = "DELETE FROM PELANGGAN_NOMOR_TELEPON_AKTIF WHERE Nomor_Telepon_Aktif = ? AND ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, noTelp);
            ps.setInt(2, idPelanggan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


        public boolean insertAlamat(AlamatKirim alamat) {
        String sql = "INSERT INTO ALAMAT_KIRIM (Label_Alamat, ID_Pelanggan, Nama_Penerima, No_Telp_Penerima, Detail_Jalan, Kota, Provinsi) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, alamat.getLabelAlamat());
            ps.setInt(2, alamat.getIdPelanggan());
            ps.setString(3, alamat.getNamaPenerima());
            ps.setString(4, alamat.getNoTelpPenerima());
            ps.setString(5, alamat.getDetailJalan());
            ps.setString(6, alamat.getKota());
            ps.setString(7, alamat.getProvinsi());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        public List<AlamatKirim> getAlamatByPelanggan(int idPelanggan) {
        List<AlamatKirim> list = new ArrayList<>();
        String sql = "SELECT * FROM ALAMAT_KIRIM WHERE ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AlamatKirim ak = new AlamatKirim(
                        rs.getString("Label_Alamat"),
                        rs.getInt("ID_Pelanggan"),
                        rs.getString("Nama_Penerima"),
                        rs.getString("No_Telp_Penerima"),
                        rs.getString("Detail_Jalan"),
                        rs.getString("Kota"),
                        rs.getString("Provinsi")
                    );
                    list.add(ak);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

        public boolean updateAlamat(AlamatKirim alamat) {
        String sql = "UPDATE ALAMAT_KIRIM SET Nama_Penerima = ?, No_Telp_Penerima = ?, Detail_Jalan = ?, Kota = ?, Provinsi = ? WHERE Label_Alamat = ? AND ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, alamat.getNamaPenerima());
            ps.setString(2, alamat.getNoTelpPenerima());
            ps.setString(3, alamat.getDetailJalan());
            ps.setString(4, alamat.getKota());
            ps.setString(5, alamat.getProvinsi());
            ps.setString(6, alamat.getLabelAlamat());
            ps.setInt(7, alamat.getIdPelanggan());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        public boolean deleteAlamat(String labelAlamat, int idPelanggan) {
        String sql = "DELETE FROM ALAMAT_KIRIM WHERE Label_Alamat = ? AND ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, labelAlamat);
            ps.setInt(2, idPelanggan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



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


    public boolean topUpSaldo(int idPelanggan, double nominal) {
        String sqlAmbilSaldo    = "SELECT Saldo_Zalora FROM PELANGGAN WHERE ID_Pelanggan = ?";
        String sqlUpdateSaldo   = "UPDATE PELANGGAN SET Saldo_Zalora = Saldo_Zalora + ? WHERE ID_Pelanggan = ?";
        String sqlInsertRiwayat =
            "INSERT INTO RIWAYAT_SALDO_ZALORA " +
            "(ID_Mutasi, ID_Pelanggan, Jenis_Mutasi, Nominal, " +
            " Saldo_Sebelum, Saldo_Sesudah, Waktu_Mutasi, Keterangan, ID_Pesanan, ID_Metode) " +
            "VALUES (?, ?, 'TOPUP', ?, ?, ?, GETDATE(), ?, NULL, NULL)";
        String sqlNextId        = "SELECT ISNULL(MAX(ID_Mutasi), 0) + 1 AS next_id FROM RIWAYAT_SALDO_ZALORA";

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

                        int nextId = 1;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlNextId)) {
                if (rs.next()) nextId = rs.getInt("next_id");
            }

                        String keterangan = "Top up Saldo Zalora";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertRiwayat)) {
                ps.setInt(1, nextId);
                ps.setInt(2, idPelanggan);
                ps.setDouble(3, nominal);
                ps.setDouble(4, saldoSebelum);
                ps.setDouble(5, saldoSesudah);
                ps.setString(6, keterangan);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Top up berhasil: +" + nominal + " | Saldo baru: " + saldoSesudah);
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
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }


    public List<Object[]> getRiwayatTopUp(int idPelanggan) {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT ID_Mutasi, Nominal, Waktu_Mutasi " +
            "FROM RIWAYAT_SALDO_ZALORA " +
            "WHERE ID_Pelanggan = ? AND Jenis_Mutasi = 'TOPUP' " +
            "ORDER BY Waktu_Mutasi DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("ID_Mutasi"),
                        rs.getDouble("Nominal"),
                        rs.getTimestamp("Waktu_Mutasi")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
