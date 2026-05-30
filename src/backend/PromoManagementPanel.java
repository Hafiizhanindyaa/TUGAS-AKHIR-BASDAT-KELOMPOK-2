package backend;

import dao.PromoDAO;
import model.Voucher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PromoManagementPanel extends JPanel {
    private PromoDAO dao;
    private JTable tblVoucher;
    private DefaultTableModel tableModel;

    private JTextField tfKode, tfNilai, tfMinBelanja, tfKuota, tfTglMulai, tfTglBerakhir;
    private JComboBox<String> cbTipe;

    public PromoManagementPanel() {
        dao = new PromoDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Manajemen Voucher & Kode Promo");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Kode Voucher", "Tipe Diskon", "Nilai Diskon", "Min. Belanja", "Tgl Mulai", "Tgl Berakhir", "Kuota"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblVoucher = new JTable(tableModel);
        tblVoucher.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tblVoucher), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 8, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Data Voucher"));

        formPanel.add(new JLabel("Kode Voucher:"));
        tfKode = new JTextField(); formPanel.add(tfKode);

        formPanel.add(new JLabel("Tipe Diskon:"));
        cbTipe = new JComboBox<>(new String[]{"PERSENTASE", "NOMINAL"});
        formPanel.add(cbTipe);

        formPanel.add(new JLabel("Nilai Diskon (% atau Rp):"));
        tfNilai = new JTextField(); formPanel.add(tfNilai);

        formPanel.add(new JLabel("Minimum Belanja (Rp):"));
        tfMinBelanja = new JTextField(); formPanel.add(tfMinBelanja);

        formPanel.add(new JLabel("Kuota Pemakaian:"));
        tfKuota = new JTextField(); formPanel.add(tfKuota);

        formPanel.add(new JLabel("Tanggal Mulai (yyyy-MM-dd):"));
        tfTglMulai = new JTextField(); formPanel.add(tfTglMulai);

        formPanel.add(new JLabel("Tanggal Berakhir (yyyy-MM-dd):"));
        tfTglBerakhir = new JTextField(); formPanel.add(tfTglBerakhir);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnTambah = new JButton("Tambah Voucher");
        JButton btnUpdate = new JButton("Update Voucher");
        JButton btnHapus = new JButton("Hapus Voucher");
        JButton btnBersih = new JButton("Bersihkan Form");
        btnHapus.setBackground(new Color(220, 53, 69)); btnHapus.setForeground(Color.WHITE);
        btnTambah.setBackground(new Color(40, 167, 69)); btnTambah.setForeground(Color.WHITE);
        btnPanel.add(btnTambah); btnPanel.add(btnUpdate); btnPanel.add(btnHapus); btnPanel.add(btnBersih);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(btnPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        tblVoucher.getSelectionModel().addListSelectionListener(e -> {
            int row = tblVoucher.getSelectedRow();
            if (row >= 0) {
                tfKode.setText(tblVoucher.getValueAt(row, 0).toString());
                tfKode.setEditable(false);
                cbTipe.setSelectedItem(tblVoucher.getValueAt(row, 1).toString());
                tfNilai.setText(tblVoucher.getValueAt(row, 2).toString());
                tfMinBelanja.setText(tblVoucher.getValueAt(row, 3).toString());
                tfTglMulai.setText(tblVoucher.getValueAt(row, 4).toString());
                tfTglBerakhir.setText(tblVoucher.getValueAt(row, 5).toString());
                tfKuota.setText(tblVoucher.getValueAt(row, 6).toString());
            }
        });

        btnTambah.addActionListener(e -> {
            try {
                Voucher v = buildVoucherFromForm();
                if (dao.insertVoucher(v)) { JOptionPane.showMessageDialog(this, "Voucher berhasil ditambahkan!"); bersihForm(); refreshTabel(); }
                else JOptionPane.showMessageDialog(this, "Gagal! Kode voucher mungkin sudah ada.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Input tidak valid!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnUpdate.addActionListener(e -> {
            if (tblVoucher.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Pilih baris voucher terlebih dahulu!"); return; }
            try {
                Voucher v = buildVoucherFromForm();
                if (dao.updateVoucher(v)) { JOptionPane.showMessageDialog(this, "Voucher berhasil diperbarui!"); bersihForm(); refreshTabel(); }
                else JOptionPane.showMessageDialog(this, "Gagal update voucher!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Input tidak valid!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnHapus.addActionListener(e -> {
            int row = tblVoucher.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih baris voucher terlebih dahulu!"); return; }
            String kode = tblVoucher.getValueAt(row, 0).toString();
            if (JOptionPane.showConfirmDialog(this, "Hapus voucher [" + kode + "]?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (dao.deleteVoucher(kode)) { JOptionPane.showMessageDialog(this, "Voucher berhasil dihapus!"); bersihForm(); refreshTabel(); }
                else JOptionPane.showMessageDialog(this, "Gagal menghapus voucher!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBersih.addActionListener(e -> bersihForm());
        refreshTabel();
    }

    private Voucher buildVoucherFromForm() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date tglMulai = sdf.parse(tfTglMulai.getText().trim());
        Date tglBerakhir = sdf.parse(tfTglBerakhir.getText().trim());
        return new Voucher(
            tfKode.getText().trim(),
            cbTipe.getSelectedItem().toString(),
            Double.parseDouble(tfNilai.getText().trim()),
            Double.parseDouble(tfMinBelanja.getText().trim()),
            tglMulai, tglBerakhir,
            Integer.parseInt(tfKuota.getText().trim())
        );
    }

    private void bersihForm() {
        tfKode.setText(""); tfKode.setEditable(true);
        tfNilai.setText(""); tfMinBelanja.setText(""); tfKuota.setText("");
        tfTglMulai.setText(""); tfTglBerakhir.setText("");
        cbTipe.setSelectedIndex(0);
        tblVoucher.clearSelection();
    }

    public void refreshTabel() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Voucher v : dao.getAllVoucher()) {
            tableModel.addRow(new Object[]{
                v.getKodeVoucher(), v.getTipeDiskon(), v.getNilaiDiskon(),
                v.getMinimumBelanja(),
                v.getTanggalMulai() != null ? sdf.format(v.getTanggalMulai()) : "-",
                v.getTanggalBerakhir() != null ? sdf.format(v.getTanggalBerakhir()) : "-",
                v.getKuotaPemakaian()
            });
        }
    }
}
