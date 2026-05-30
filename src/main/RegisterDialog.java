package main;

import dao.UserDAO;
import model.Pelanggan;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterDialog extends JDialog {

    private JTextField tfNamaAkun;
    private JTextField tfSurel;
    private JPasswordField pfKataSandi;
    private JPasswordField pfKonfirmasi;
    private JTextField tfTanggalLahir;
    private JTextField tfNomorTelepon;

    private JButton btnDaftar;
    private JButton btnBatal;

    private boolean registrasiSukses = false;
    private String namaAkunTerdaftar  = "";

    private UserDAO userDAO;

    public RegisterDialog(Frame parent) {
        super(parent, "Daftar Akun Baru - Zalora", true);
        this.userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        setSize(430, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JLabel lblHeader = new JLabel("Buat Akun Pelanggan Baru", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 16));
        lblHeader.setForeground(new Color(40, 167, 69));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(20, 10, 5, 10));
        add(lblHeader, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 12));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        formPanel.add(buat("Nama Akun *:"));
        tfNamaAkun = new JTextField();
        formPanel.add(tfNamaAkun);

        formPanel.add(buat("Surel (Email) *:"));
        tfSurel = new JTextField();
        formPanel.add(tfSurel);

        formPanel.add(buat("Kata Sandi *:"));
        pfKataSandi = new JPasswordField();
        formPanel.add(pfKataSandi);

        formPanel.add(buat("Konfirmasi Sandi *:"));
        pfKonfirmasi = new JPasswordField();
        formPanel.add(pfKonfirmasi);

        formPanel.add(buat("Tanggal Lahir *:"));
        tfTanggalLahir = new JTextField("dd/MM/yyyy");
        tfTanggalLahir.setForeground(Color.GRAY);
        tfTanggalLahir.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tfTanggalLahir.getText().equals("dd/MM/yyyy")) {
                    tfTanggalLahir.setText("");
                    tfTanggalLahir.setForeground(Color.BLACK);
                }
            }
        });
        formPanel.add(tfTanggalLahir);

        formPanel.add(buat("No. Telepon:"));
        tfNomorTelepon = new JTextField();
        formPanel.add(tfNomorTelepon);

        add(formPanel, BorderLayout.CENTER);

        JLabel lblInfo = new JLabel("* Saldo Zalora awal: Rp 0 (Top up setelah login)", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);

        btnDaftar = new JButton("Daftar Sekarang");
        btnDaftar.setBackground(new Color(40, 167, 69));
        btnDaftar.setForeground(Color.WHITE);
        btnDaftar.setFont(new Font("Arial", Font.BOLD, 13));
        btnBatal  = new JButton("Batal");

        JPanel panelSouth = new JPanel(new BorderLayout(5, 5));
        JPanel panelBtn   = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBtn.add(btnDaftar);
        panelBtn.add(btnBatal);
        panelSouth.add(lblInfo, BorderLayout.NORTH);
        panelSouth.add(panelBtn, BorderLayout.CENTER);
        panelSouth.setBorder(BorderFactory.createEmptyBorder(0, 10, 15, 10));
        add(panelSouth, BorderLayout.SOUTH);

        btnDaftar.addActionListener(e -> prosesRegistrasi());
        btnBatal.addActionListener(e  -> dispose());
    }


    private JLabel buat(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        return lbl;
    }


    private void prosesRegistrasi() {
        String namaAkun       = tfNamaAkun.getText().trim();
        String surel          = tfSurel.getText().trim();
        String kataSandi      = new String(pfKataSandi.getPassword());
        String konfirmasi     = new String(pfKonfirmasi.getPassword());
        String tglLahirStr    = tfTanggalLahir.getText().trim();
        String nomorTelepon   = tfNomorTelepon.getText().trim();

        if (namaAkun.isEmpty()) {
            tampilPeringatan("Nama Akun tidak boleh kosong!"); return;
        }
        if (surel.isEmpty()) {
            tampilPeringatan("Surel (Email) tidak boleh kosong!"); return;
        }
        if (!surel.contains("@")) {
            tampilPeringatan("Format surel tidak valid! Harus mengandung karakter '@'."); return;
        }
        if (kataSandi.isEmpty()) {
            tampilPeringatan("Kata Sandi tidak boleh kosong!"); return;
        }
        if (kataSandi.length() < 4) {
            tampilPeringatan("Kata Sandi minimal 4 karakter!"); return;
        }
        if (!kataSandi.equals(konfirmasi)) {
            tampilPeringatan("Kata Sandi dan Konfirmasi Sandi tidak sama!"); return;
        }
        if (tglLahirStr.isEmpty() || tglLahirStr.equals("dd/MM/yyyy")) {
            tampilPeringatan("Tanggal Lahir tidak boleh kosong!"); return;
        }

        Date tanggalLahir = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            tanggalLahir = sdf.parse(tglLahirStr);
        } catch (ParseException ex) {
            tampilPeringatan("Format Tanggal Lahir tidak valid!\nGunakan format: dd/MM/yyyy\nContoh: 15/08/2000");
            return;
        }

        if (userDAO.isNamaAkunExists(namaAkun)) {
            tampilPeringatan("Nama Akun \"" + namaAkun + "\" sudah digunakan!\nSilakan pilih nama akun lain."); return;
        }
        if (userDAO.isSurelExists(surel)) {
            tampilPeringatan("Surel \"" + surel + "\" sudah terdaftar!\nGunakan surel lain atau langsung login."); return;
        }
        if (!nomorTelepon.isEmpty() && userDAO.isNomorTeleponExists(nomorTelepon)) {
            tampilPeringatan("Nomor Telepon \"" + nomorTelepon + "\" sudah terdaftar!\nGunakan nomor telepon lain."); return;
        }

        int nextId = userDAO.getNextIdPelanggan();
        Pelanggan pelangganBaru = new Pelanggan();
        pelangganBaru.setIdPelanggan(nextId);
        pelangganBaru.setNamaAkun(namaAkun);
        pelangganBaru.setSurel(surel);
        pelangganBaru.setKataSandi(kataSandi);
        pelangganBaru.setTanggalLahir(tanggalLahir);

        boolean berhasil = userDAO.registerPelangganDenganTelepon(pelangganBaru, nomorTelepon);

        if (berhasil) {
            registrasiSukses   = true;
            namaAkunTerdaftar  = namaAkun;
            JOptionPane.showMessageDialog(this,
                "Akun berhasil dibuat!\n\n"
                + "Nama Akun : " + namaAkun + "\n"
                + "Surel     : " + surel + "\n"
                + "Saldo Awal: Rp 0 (Top up setelah login)\n\n"
                + "Silakan login dengan akun baru Anda.",
                "Registrasi Berhasil", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Gagal menyimpan akun ke database!\n"
                + "Periksa koneksi ke SQL Server.",
                "Error Registrasi", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void tampilPeringatan(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
    }


    public boolean isRegistrasiSukses() { return registrasiSukses; }


    public String getNamaAkunTerdaftar() { return namaAkunTerdaftar; }
}
