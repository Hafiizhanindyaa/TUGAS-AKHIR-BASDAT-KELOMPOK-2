package main;

import javax.swing.*;

import config.DBConnection;
import config.SessionManager;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDialog extends JDialog {
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JButton btnLogin, btnBatal, btnDaftar;
    private boolean loginSuccess = false;
    private String loginType;

        public LoginDialog(Frame parent, String type) {
        super(parent, "Login " + type + " - Zalora System", true);
        this.loginType = type;
        initUI(parent);
    }

        private void initUI(Frame parent) {
        int tinggi = loginType.equalsIgnoreCase("PEMBELI") ? 260 : 220;
        setSize(380, tinggi);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JLabel lblHeader = new JLabel("Silahkan Masukkan Kredensial Anda", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 14));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(lblHeader, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridLayout(2, 2, 10, 15));
        panelForm.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        panelForm.add(new JLabel("Nama Akun / Surel:"));
        tfUsername = new JTextField();
        panelForm.add(tfUsername);

        panelForm.add(new JLabel("Kata Sandi:"));
        pfPassword = new JPasswordField();
        panelForm.add(pfPassword);
        add(panelForm, BorderLayout.CENTER);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnLogin = new JButton("Masuk");
        btnBatal = new JButton("Batal");
        btnLogin.setBackground(new Color(40, 167, 69));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 12));
        panelButtons.add(btnLogin);
        panelButtons.add(btnBatal);

        if (loginType.equalsIgnoreCase("PEMBELI")) {
            JPanel panelSouth = new JPanel(new BorderLayout());
            panelSouth.add(panelButtons, BorderLayout.NORTH);

                        JPanel panelDaftar = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panelDaftar.add(new JLabel("Belum punya akun?"));
            btnDaftar = new JButton("Daftar Akun Baru");
            btnDaftar.setForeground(new Color(0, 102, 204));
            btnDaftar.setBorderPainted(false);
            btnDaftar.setContentAreaFilled(false);
            btnDaftar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDaftar.setFont(new Font("Arial", Font.BOLD, 12));
            panelDaftar.add(btnDaftar);
            panelSouth.add(panelDaftar, BorderLayout.SOUTH);
            add(panelSouth, BorderLayout.SOUTH);
        } else {
            add(panelButtons, BorderLayout.SOUTH);
        }

        btnLogin.addActionListener(e -> {
            String inputUser = tfUsername.getText().trim();
            String inputPass = new String(pfPassword.getPassword());

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (loginType.equalsIgnoreCase("ADMIN")) {
                prosesLoginAdmin(inputUser, inputPass);
            } else {
                prosesLoginPembeli(inputUser, inputPass);
            }
        });

        btnBatal.addActionListener(e -> dispose());

        if (loginType.equalsIgnoreCase("PEMBELI") && btnDaftar != null) {
            btnDaftar.addActionListener(e -> {
                                RegisterDialog registerDlg = new RegisterDialog((Frame) getParent());
                registerDlg.setVisible(true);
                                if (registerDlg.isRegistrasiSukses()) {
                    tfUsername.setText(registerDlg.getNamaAkunTerdaftar());
                    pfPassword.setText("");
                    pfPassword.requestFocus();
                    JOptionPane.showMessageDialog(this,
                        "Akun berhasil dibuat! Silakan masukkan kata sandi Anda.",
                        "Registrasi Berhasil", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }
    }

        private void prosesLoginAdmin(String user, String pass) {
        if (user.equals("admin") && pass.equals("zalora2")) {
            loginSuccess = true;
            SessionManager.startSession(0, "Administrator", "ADMIN");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Username atau Password Admin Salah!", "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

        private void prosesLoginPembeli(String user, String pass) {
        String sql = "SELECT ID_Pelanggan, Nama_Akun FROM PELANGGAN WHERE (Nama_Akun = ? OR Surel = ?) AND Kata_Sandi = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user);
            ps.setString(2, user);
            ps.setString(3, pass);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idPelanggan = rs.getInt("ID_Pelanggan");
                    String namaAkun = rs.getString("Nama_Akun");
                    SessionManager.startSession(idPelanggan, namaAkun, "PEMBELI");
                    loginSuccess = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Akun Pelanggan tidak ditemukan atau password salah!", "Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke Database SQL Server!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }
}
