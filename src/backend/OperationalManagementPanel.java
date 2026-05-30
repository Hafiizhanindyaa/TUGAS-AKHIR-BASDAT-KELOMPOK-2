package backend;

import dao.OperationalDAO;
import model.Kurir;
import model.Kategori;
import model.MetodePembayaran;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OperationalManagementPanel extends JPanel {
    private OperationalDAO dao;
    private JTabbedPane tabbedPane;

        private JTable tblKurir;
    private DefaultTableModel mdlKurir;
    private JTextField tfKurirId, tfKurirNama, tfKurirLayanan, tfKurirEstimasi;

        private JTable tblKategori;
    private DefaultTableModel mdlKategori;
    private JTextField tfKatId, tfKatNama, tfKatGender;

        private JTable tblMetode;
    private DefaultTableModel mdlMetode;
    private JTextField tfMetId, tfMetNama, tfMetTipe, tfMetProvider;

    public OperationalManagementPanel() {
        dao = new OperationalDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Manajemen Data Operasional");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Kurir & Ekspedisi", buildKurirTab());
        tabbedPane.addTab("Kategori Produk", buildKategoriTab());
        tabbedPane.addTab("Metode Pembayaran", buildMetodeTab());
        add(tabbedPane, BorderLayout.CENTER);

        refreshKurir();
        refreshKategori();
        refreshMetode();
    }

        private JPanel buildKurirTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Nama Ekspedisi", "Jenis Layanan", "Estimasi Waktu"};
        mdlKurir = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblKurir = new JTable(mdlKurir);
        tblKurir.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tblKurir), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 6, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Data Kurir"));
        formPanel.add(new JLabel("ID Kurir:")); tfKurirId = new JTextField(); formPanel.add(tfKurirId);
        formPanel.add(new JLabel("Nama Ekspedisi:")); tfKurirNama = new JTextField(); formPanel.add(tfKurirNama);
        formPanel.add(new JLabel("Jenis Layanan:")); tfKurirLayanan = new JTextField(); formPanel.add(tfKurirLayanan);
        formPanel.add(new JLabel("Estimasi Waktu:")); tfKurirEstimasi = new JTextField(); formPanel.add(tfKurirEstimasi);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnTambah = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus = new JButton("Hapus");
        JButton btnBersih = new JButton("Bersihkan Form");
        btnHapus.setBackground(new Color(220, 53, 69)); btnHapus.setForeground(Color.WHITE);
        btnPanel.add(btnTambah); btnPanel.add(btnUpdate); btnPanel.add(btnHapus); btnPanel.add(btnBersih);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(btnPanel, BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);

        tblKurir.getSelectionModel().addListSelectionListener(e -> {
            int row = tblKurir.getSelectedRow();
            if (row >= 0) {
                tfKurirId.setText(tblKurir.getValueAt(row, 0).toString());
                tfKurirNama.setText(tblKurir.getValueAt(row, 1).toString());
                tfKurirLayanan.setText(tblKurir.getValueAt(row, 2).toString());
                tfKurirEstimasi.setText(tblKurir.getValueAt(row, 3).toString());
                tfKurirId.setEditable(false);
            }
        });

        btnTambah.addActionListener(e -> {
            try {
                Kurir k = new Kurir(Integer.parseInt(tfKurirId.getText().trim()),
                    tfKurirNama.getText().trim(), tfKurirLayanan.getText().trim(), tfKurirEstimasi.getText().trim());
                if (dao.insertKurir(k)) { JOptionPane.showMessageDialog(this, "Kurir berhasil ditambahkan!"); bersihKurir(); refreshKurir(); }
                else JOptionPane.showMessageDialog(this, "Gagal menambah kurir!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Input tidak valid!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnUpdate.addActionListener(e -> {
            if (tblKurir.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Pilih baris terlebih dahulu!"); return; }
            try {
                Kurir k = new Kurir(Integer.parseInt(tfKurirId.getText().trim()),
                    tfKurirNama.getText().trim(), tfKurirLayanan.getText().trim(), tfKurirEstimasi.getText().trim());
                if (dao.updateKurir(k)) { JOptionPane.showMessageDialog(this, "Data kurir berhasil diperbarui!"); bersihKurir(); refreshKurir(); }
                else JOptionPane.showMessageDialog(this, "Gagal update kurir!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Input tidak valid!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnHapus.addActionListener(e -> {
            int row = tblKurir.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih baris terlebih dahulu!"); return; }
            int id = Integer.parseInt(tblKurir.getValueAt(row, 0).toString());
            if (JOptionPane.showConfirmDialog(this, "Hapus kurir ID " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (dao.deleteKurir(id)) { JOptionPane.showMessageDialog(this, "Kurir berhasil dihapus!"); bersihKurir(); refreshKurir(); }
                else JOptionPane.showMessageDialog(this, "Gagal menghapus kurir!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBersih.addActionListener(e -> bersihKurir());
        return panel;
    }

    private void bersihKurir() {
        tfKurirId.setText(""); tfKurirId.setEditable(true);
        tfKurirNama.setText(""); tfKurirLayanan.setText(""); tfKurirEstimasi.setText("");
        tblKurir.clearSelection();
    }

    public void refreshKurir() {
        mdlKurir.setRowCount(0);
        for (Kurir k : dao.getAllKurir())
            mdlKurir.addRow(new Object[]{k.getIdKurir(), k.getNamaEkspedisi(), k.getJenisLayanan(), k.getEstimasiWaktu()});
    }

        private JPanel buildKategoriTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Nama Kategori", "Target Gender"};
        mdlKategori = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblKategori = new JTable(mdlKategori);
        tblKategori.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tblKategori), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 6, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Kategori"));
        formPanel.add(new JLabel("ID Kategori:")); tfKatId = new JTextField(); formPanel.add(tfKatId);
        formPanel.add(new JLabel("Nama Kategori:")); tfKatNama = new JTextField(); formPanel.add(tfKatNama);
        formPanel.add(new JLabel("Target Gender (Pria/Wanita/Unisex):")); tfKatGender = new JTextField(); formPanel.add(tfKatGender);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnTambah = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus = new JButton("Hapus");
        JButton btnBersih = new JButton("Bersihkan Form");
        btnHapus.setBackground(new Color(220, 53, 69)); btnHapus.setForeground(Color.WHITE);
        btnPanel.add(btnTambah); btnPanel.add(btnUpdate); btnPanel.add(btnHapus); btnPanel.add(btnBersih);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(btnPanel, BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);

        tblKategori.getSelectionModel().addListSelectionListener(e -> {
            int row = tblKategori.getSelectedRow();
            if (row >= 0) {
                tfKatId.setText(tblKategori.getValueAt(row, 0).toString());
                tfKatNama.setText(tblKategori.getValueAt(row, 1).toString());
                tfKatGender.setText(tblKategori.getValueAt(row, 2).toString());
                tfKatId.setEditable(false);
            }
        });

        btnTambah.addActionListener(e -> {
            try {
                Kategori k = new Kategori(Integer.parseInt(tfKatId.getText().trim()), tfKatNama.getText().trim(), tfKatGender.getText().trim());
                if (dao.insertKategori(k)) { JOptionPane.showMessageDialog(this, "Kategori berhasil ditambahkan!"); bersihKategori(); refreshKategori(); }
                else JOptionPane.showMessageDialog(this, "Gagal menambah kategori!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Input tidak valid!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnUpdate.addActionListener(e -> {
            if (tblKategori.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Pilih baris terlebih dahulu!"); return; }
            try {
                Kategori k = new Kategori(Integer.parseInt(tfKatId.getText().trim()), tfKatNama.getText().trim(), tfKatGender.getText().trim());
                if (dao.updateKategori(k)) { JOptionPane.showMessageDialog(this, "Kategori berhasil diperbarui!"); bersihKategori(); refreshKategori(); }
                else JOptionPane.showMessageDialog(this, "Gagal update kategori!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Input tidak valid!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnHapus.addActionListener(e -> {
            int row = tblKategori.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih baris terlebih dahulu!"); return; }
            int id = Integer.parseInt(tblKategori.getValueAt(row, 0).toString());
            if (JOptionPane.showConfirmDialog(this, "Hapus kategori ID " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (dao.deleteKategori(id)) { JOptionPane.showMessageDialog(this, "Kategori berhasil dihapus!"); bersihKategori(); refreshKategori(); }
                else JOptionPane.showMessageDialog(this, "Gagal menghapus! Mungkin masih ada produk yang menggunakan kategori ini.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBersih.addActionListener(e -> bersihKategori());
        return panel;
    }

    private void bersihKategori() {
        tfKatId.setText(""); tfKatId.setEditable(true);
        tfKatNama.setText(""); tfKatGender.setText("");
        tblKategori.clearSelection();
    }

    public void refreshKategori() {
        mdlKategori.setRowCount(0);
        for (Kategori k : dao.getAllKategori())
            mdlKategori.addRow(new Object[]{k.getIdKategori(), k.getNamaKategori(), k.getTargetGender()});
    }

        private JPanel buildMetodeTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Nama Metode", "Tipe", "Provider"};
        mdlMetode = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblMetode = new JTable(mdlMetode);
        tblMetode.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tblMetode), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 6, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Metode Pembayaran"));
        formPanel.add(new JLabel("ID Metode:")); tfMetId = new JTextField(); formPanel.add(tfMetId);
        formPanel.add(new JLabel("Nama Metode:")); tfMetNama = new JTextField(); formPanel.add(tfMetNama);
        formPanel.add(new JLabel("Tipe Metode:")); tfMetTipe = new JTextField(); formPanel.add(tfMetTipe);
        formPanel.add(new JLabel("Nama Provider:")); tfMetProvider = new JTextField(); formPanel.add(tfMetProvider);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnTambah = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus = new JButton("Hapus");
        JButton btnBersih = new JButton("Bersihkan Form");
        btnHapus.setBackground(new Color(220, 53, 69)); btnHapus.setForeground(Color.WHITE);
        btnPanel.add(btnTambah); btnPanel.add(btnUpdate); btnPanel.add(btnHapus); btnPanel.add(btnBersih);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(btnPanel, BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);

        tblMetode.getSelectionModel().addListSelectionListener(e -> {
            int row = tblMetode.getSelectedRow();
            if (row >= 0) {
                tfMetId.setText(tblMetode.getValueAt(row, 0).toString());
                tfMetNama.setText(tblMetode.getValueAt(row, 1).toString());
                tfMetTipe.setText(tblMetode.getValueAt(row, 2).toString());
                tfMetProvider.setText(tblMetode.getValueAt(row, 3).toString());
                tfMetId.setEditable(false);
            }
        });

        btnTambah.addActionListener(e -> {
            try {
                MetodePembayaran m = new MetodePembayaran(Integer.parseInt(tfMetId.getText().trim()),
                    tfMetNama.getText().trim(), tfMetTipe.getText().trim(), tfMetProvider.getText().trim());
                if (dao.insertMetode(m)) { JOptionPane.showMessageDialog(this, "Metode pembayaran berhasil ditambahkan!"); bersihMetode(); refreshMetode(); }
                else JOptionPane.showMessageDialog(this, "Gagal menambah metode!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Input tidak valid!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnUpdate.addActionListener(e -> {
            if (tblMetode.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Pilih baris terlebih dahulu!"); return; }
            try {
                MetodePembayaran m = new MetodePembayaran(Integer.parseInt(tfMetId.getText().trim()),
                    tfMetNama.getText().trim(), tfMetTipe.getText().trim(), tfMetProvider.getText().trim());
                if (dao.updateMetode(m)) { JOptionPane.showMessageDialog(this, "Metode pembayaran berhasil diperbarui!"); bersihMetode(); refreshMetode(); }
                else JOptionPane.showMessageDialog(this, "Gagal update metode!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Input tidak valid!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnHapus.addActionListener(e -> {
            int row = tblMetode.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih baris terlebih dahulu!"); return; }
            int id = Integer.parseInt(tblMetode.getValueAt(row, 0).toString());
            if (JOptionPane.showConfirmDialog(this, "Hapus metode ID " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (dao.deleteMetode(id)) { JOptionPane.showMessageDialog(this, "Metode berhasil dihapus!"); bersihMetode(); refreshMetode(); }
                else JOptionPane.showMessageDialog(this, "Gagal menghapus! Mungkin masih digunakan dalam pesanan.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBersih.addActionListener(e -> bersihMetode());
        return panel;
    }

    private void bersihMetode() {
        tfMetId.setText(""); tfMetId.setEditable(true);
        tfMetNama.setText(""); tfMetTipe.setText(""); tfMetProvider.setText("");
        tblMetode.clearSelection();
    }

    public void refreshMetode() {
        mdlMetode.setRowCount(0);
        for (MetodePembayaran m : dao.getAllMetode())
            mdlMetode.addRow(new Object[]{m.getIdMetode(), m.getNamaMetode(), m.getTipeMetode(), m.getNamaProvider()});
    }
}
