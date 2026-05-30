package frontend;

import config.SessionManager;
import dao.UserDAO;
import model.AlamatKirim;
import model.Pelanggan;
import model.PelangganNomorTeleponAktif;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ProfilPanel extends JPanel {

    private UserDAO userDAO;
    private JTabbedPane tabbedPane;

        private JTextField tfNama, tfSurel, tfTglLahir;
    private JPasswordField tfSandi;

        private JTable tblAlamat;
    private DefaultTableModel mdlAlamat;
    private JTextField tfLabel, tfNamaPenerima, tfNoTelpPenerima, tfJalan, tfKota, tfProvinsi;

        private JTable tblTelp;
    private DefaultTableModel mdlTelp;
    private JTextField tfNoTelp;

    public ProfilPanel() {
        this.userDAO = new UserDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Profil & Pengaturan Akun");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Data Akun", buildTabAkun());
        tabbedPane.addTab("Alamat Pengiriman", buildTabAlamat());
        tabbedPane.addTab("Nomor Telepon", buildTabTelepon());
        add(tabbedPane, BorderLayout.CENTER);

        muatData();
    }

        private JPanel buildTabAkun() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nama Akun:"), gbc);
        gbc.gridx = 1; tfNama = new JTextField(25); panel.add(tfNama, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Surel (Email):"), gbc);
        gbc.gridx = 1; tfSurel = new JTextField(25); panel.add(tfSurel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Kata Sandi Baru:"), gbc);
        gbc.gridx = 1; tfSandi = new JPasswordField(25); panel.add(tfSandi, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Tanggal Lahir (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; tfTglLahir = new JTextField(25); panel.add(tfTglLahir, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblIdInfo = new JLabel("ID Pelanggan: " + SessionManager.getIdPelanggan());
        lblIdInfo.setForeground(Color.GRAY);
        lblIdInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        gbc.gridwidth = 2;
        panel.add(lblIdInfo, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 1; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        JButton btnSimpan = new JButton("Simpan Perubahan");
        btnSimpan.setBackground(new Color(0, 123, 255));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(btnSimpan, gbc);

        btnSimpan.addActionListener(e -> simpanDataAkun());

        return panel;
    }

    private void simpanDataAkun() {
        try {
            String nama = tfNama.getText().trim();
            String surel = tfSurel.getText().trim();
            String sandi = new String(tfSandi.getPassword()).trim();
            String tglStr = tfTglLahir.getText().trim();

            if (nama.isEmpty() || surel.isEmpty() || tglStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama, surel, dan tanggal lahir wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date tglLahir = sdf.parse(tglStr);

                        if (sandi.isEmpty()) {
                sandi = getKataSandiLama();
            }

            Pelanggan p = new Pelanggan(
                SessionManager.getIdPelanggan(), nama, surel, sandi,
                new java.sql.Date(tglLahir.getTime())
            );

            if (userDAO.updatePelanggan(p)) {
                                SessionManager.startSession(SessionManager.getIdPelanggan(), nama, SessionManager.getRole());
                JOptionPane.showMessageDialog(this, "Data akun berhasil diperbarui!");
                tfSandi.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui data akun!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Format tanggal tidak valid! Gunakan yyyy-MM-dd\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getKataSandiLama() {
        List<Pelanggan> semua = userDAO.getAllPelanggan();
        for (Pelanggan p : semua) {
            if (p.getIdPelanggan() == SessionManager.getIdPelanggan()) {
                return p.getKataSandi();
            }
        }
        return "";
    }

        private JPanel buildTabAlamat() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Label", "Nama Penerima", "No. Telp", "Jalan", "Kota", "Provinsi"};
        mdlAlamat = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblAlamat = new JTable(mdlAlamat);
        tblAlamat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tblAlamat), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 6, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Alamat Pengiriman"));
        formPanel.add(new JLabel("Label Alamat (unik):")); tfLabel = new JTextField(); formPanel.add(tfLabel);
        formPanel.add(new JLabel("Nama Penerima:")); tfNamaPenerima = new JTextField(); formPanel.add(tfNamaPenerima);
        formPanel.add(new JLabel("No. Telp Penerima:")); tfNoTelpPenerima = new JTextField(); formPanel.add(tfNoTelpPenerima);
        formPanel.add(new JLabel("Detail Jalan:")); tfJalan = new JTextField(); formPanel.add(tfJalan);
        formPanel.add(new JLabel("Kota:")); tfKota = new JTextField(); formPanel.add(tfKota);
        formPanel.add(new JLabel("Provinsi:")); tfProvinsi = new JTextField(); formPanel.add(tfProvinsi);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnTambah = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus  = new JButton("Hapus");
        JButton btnBersih = new JButton("Bersihkan");
        btnTambah.setBackground(new Color(40, 167, 69)); btnTambah.setForeground(Color.WHITE);
        btnHapus.setBackground(new Color(220, 53, 69));  btnHapus.setForeground(Color.WHITE);
        btnPanel.add(btnTambah); btnPanel.add(btnUpdate); btnPanel.add(btnHapus); btnPanel.add(btnBersih);

        JPanel south = new JPanel(new BorderLayout());
        south.add(formPanel, BorderLayout.CENTER);
        south.add(btnPanel, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);

        tblAlamat.getSelectionModel().addListSelectionListener(e -> {
            int row = tblAlamat.getSelectedRow();
            if (row >= 0) {
                tfLabel.setText(tblAlamat.getValueAt(row, 0).toString());
                tfLabel.setEditable(false);
                tfNamaPenerima.setText(tblAlamat.getValueAt(row, 1).toString());
                tfNoTelpPenerima.setText(tblAlamat.getValueAt(row, 2).toString());
                tfJalan.setText(tblAlamat.getValueAt(row, 3).toString());
                tfKota.setText(tblAlamat.getValueAt(row, 4).toString());
                tfProvinsi.setText(tblAlamat.getValueAt(row, 5).toString());
            }
        });

        btnTambah.addActionListener(e -> {
            AlamatKirim a = buildAlamatFromForm();
            if (a == null) return;
            if (userDAO.insertAlamat(a)) {
                JOptionPane.showMessageDialog(this, "Alamat berhasil ditambahkan!");
                bersihFormAlamat(); refreshAlamat();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal! Label alamat mungkin sudah ada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnUpdate.addActionListener(e -> {
            if (tblAlamat.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Pilih alamat terlebih dahulu!"); return; }
            AlamatKirim a = buildAlamatFromForm();
            if (a == null) return;
            if (userDAO.updateAlamat(a)) {
                JOptionPane.showMessageDialog(this, "Alamat berhasil diperbarui!");
                bersihFormAlamat(); refreshAlamat();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui alamat!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnHapus.addActionListener(e -> {
            int row = tblAlamat.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih alamat terlebih dahulu!"); return; }
            String label = tblAlamat.getValueAt(row, 0).toString();
            if (JOptionPane.showConfirmDialog(this, "Hapus alamat \"" + label + "\"?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (userDAO.deleteAlamat(label, SessionManager.getIdPelanggan())) {
                    JOptionPane.showMessageDialog(this, "Alamat berhasil dihapus!");
                    bersihFormAlamat(); refreshAlamat();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus alamat!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnBersih.addActionListener(e -> bersihFormAlamat());
        return panel;
    }

    private AlamatKirim buildAlamatFromForm() {
        String label = tfLabel.getText().trim();
        String nama  = tfNamaPenerima.getText().trim();
        String telp  = tfNoTelpPenerima.getText().trim();
        String jalan = tfJalan.getText().trim();
        String kota  = tfKota.getText().trim();
        String prov  = tfProvinsi.getText().trim();
        if (label.isEmpty() || nama.isEmpty() || jalan.isEmpty() || kota.isEmpty() || prov.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return new AlamatKirim(label, SessionManager.getIdPelanggan(), nama, telp, jalan, kota, prov);
    }

    private void bersihFormAlamat() {
        tfLabel.setText(""); tfLabel.setEditable(true);
        tfNamaPenerima.setText(""); tfNoTelpPenerima.setText("");
        tfJalan.setText(""); tfKota.setText(""); tfProvinsi.setText("");
        tblAlamat.clearSelection();
    }

    private void refreshAlamat() {
        mdlAlamat.setRowCount(0);
        for (AlamatKirim a : userDAO.getAlamatByPelanggan(SessionManager.getIdPelanggan())) {
            mdlAlamat.addRow(new Object[]{
                a.getLabelAlamat(), a.getNamaPenerima(), a.getNoTelpPenerima(),
                a.getDetailJalan(), a.getKota(), a.getProvinsi()
            });
        }
    }

        private JPanel buildTabTelepon() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Nomor Telepon Aktif"};
        mdlTelp = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblTelp = new JTable(mdlTelp);
        tblTelp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tblTelp), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(1, 2, 6, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Nomor Telepon"));
        formPanel.add(new JLabel("Nomor Telepon:"));
        tfNoTelp = new JTextField();
        formPanel.add(tfNoTelp);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnTambah = new JButton("Tambah");
        JButton btnHapus  = new JButton("Hapus Terpilih");
        JButton btnBersih = new JButton("Bersihkan");
        btnTambah.setBackground(new Color(40, 167, 69)); btnTambah.setForeground(Color.WHITE);
        btnHapus.setBackground(new Color(220, 53, 69));  btnHapus.setForeground(Color.WHITE);
        btnPanel.add(btnTambah); btnPanel.add(btnHapus); btnPanel.add(btnBersih);

        JLabel lblInfo = new JLabel("  * Satu pelanggan dapat memiliki lebih dari satu nomor telepon aktif.");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);

        JPanel south = new JPanel(new BorderLayout());
        south.add(formPanel, BorderLayout.NORTH);
        south.add(btnPanel, BorderLayout.CENTER);
        south.add(lblInfo, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);

        tblTelp.getSelectionModel().addListSelectionListener(e -> {
            int row = tblTelp.getSelectedRow();
            if (row >= 0) tfNoTelp.setText(tblTelp.getValueAt(row, 0).toString());
        });

        btnTambah.addActionListener(e -> {
            String noTelp = tfNoTelp.getText().trim();
            if (noTelp.isEmpty()) { JOptionPane.showMessageDialog(this, "Nomor telepon tidak boleh kosong!"); return; }
            PelangganNomorTeleponAktif nt = new PelangganNomorTeleponAktif(noTelp, SessionManager.getIdPelanggan());
            if (userDAO.insertNoTelp(nt)) {
                JOptionPane.showMessageDialog(this, "Nomor telepon berhasil ditambahkan!");
                tfNoTelp.setText(""); refreshTelepon();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal! Nomor mungkin sudah terdaftar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnHapus.addActionListener(e -> {
            int row = tblTelp.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih nomor yang ingin dihapus!"); return; }
            String noTelp = tblTelp.getValueAt(row, 0).toString();
            if (JOptionPane.showConfirmDialog(this, "Hapus nomor " + noTelp + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (userDAO.deleteNoTelp(noTelp, SessionManager.getIdPelanggan())) {
                    JOptionPane.showMessageDialog(this, "Nomor berhasil dihapus!");
                    tfNoTelp.setText(""); refreshTelepon();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus nomor!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnBersih.addActionListener(e -> { tfNoTelp.setText(""); tblTelp.clearSelection(); });
        return panel;
    }

    private void refreshTelepon() {
        mdlTelp.setRowCount(0);
        for (PelangganNomorTeleponAktif nt : userDAO.getNoTelpByPelanggan(SessionManager.getIdPelanggan())) {
            mdlTelp.addRow(new Object[]{ nt.getNomorTeleponAktif() });
        }
    }

        public void muatData() {
                List<Pelanggan> semua = userDAO.getAllPelanggan();
        for (Pelanggan p : semua) {
            if (p.getIdPelanggan() == SessionManager.getIdPelanggan()) {
                tfNama.setText(p.getNamaAkun());
                tfSurel.setText(p.getSurel());
                if (p.getTanggalLahir() != null) {
                    tfTglLahir.setText(new SimpleDateFormat("yyyy-MM-dd").format(p.getTanggalLahir()));
                }
                tfSandi.setText("");
                break;
            }
        }
        refreshAlamat();
        refreshTelepon();
    }
}