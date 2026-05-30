package backend;

import dao.InventoryDAO;
import model.VarianProduk;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VarianManagementDialog extends JDialog {
    private InventoryDAO dao;
    private String skuProduk;
    private JTable tblVarian;
    private DefaultTableModel tableModel;
    private JTextField tfIdVarian, tfWarna, tfUkuran, tfStok;

    public VarianManagementDialog(Frame parent, String sku, String namaProduk) {
        super(parent, "Kelola Varian & Stok: " + namaProduk + " [" + sku + "]", true);
        this.skuProduk = sku;
        this.dao = new InventoryDAO();
        initUI();
    }

    private void initUI() {
        setSize(600, 450);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        String[] cols = {"ID Varian", "Warna", "Ukuran", "Stok"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblVarian = new JTable(tableModel);
        tblVarian.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tblVarian), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 6, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Varian"));
        formPanel.add(new JLabel("ID Varian:")); tfIdVarian = new JTextField(); formPanel.add(tfIdVarian);
        formPanel.add(new JLabel("Warna:")); tfWarna = new JTextField(); formPanel.add(tfWarna);
        formPanel.add(new JLabel("Ukuran (S/M/L/XL/38/39...):")); tfUkuran = new JTextField(); formPanel.add(tfUkuran);
        formPanel.add(new JLabel("Stok:")); tfStok = new JTextField(); formPanel.add(tfStok);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnTambah = new JButton("Tambah Varian");
        JButton btnUpdateStok = new JButton("Update Stok");
        JButton btnTutup = new JButton("Tutup");
        btnTambah.setBackground(new Color(40, 167, 69)); btnTambah.setForeground(Color.WHITE);
        btnUpdateStok.setBackground(new Color(255, 193, 7));
        btnPanel.add(btnTambah); btnPanel.add(btnUpdateStok); btnPanel.add(btnTutup);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(btnPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        tblVarian.getSelectionModel().addListSelectionListener(e -> {
            int row = tblVarian.getSelectedRow();
            if (row >= 0) {
                tfIdVarian.setText(tblVarian.getValueAt(row, 0).toString());
                tfIdVarian.setEditable(false);
                tfWarna.setText(tblVarian.getValueAt(row, 1).toString());
                tfUkuran.setText(tblVarian.getValueAt(row, 2).toString());
                tfStok.setText(tblVarian.getValueAt(row, 3).toString());
            }
        });

        btnTambah.addActionListener(e -> {
            try {
                VarianProduk vp = new VarianProduk(
                    Integer.parseInt(tfIdVarian.getText().trim()),
                    tfWarna.getText().trim(), tfUkuran.getText().trim(),
                    Integer.parseInt(tfStok.getText().trim()), skuProduk
                );
                if (dao.insertVarian(vp)) { JOptionPane.showMessageDialog(this, "Varian berhasil ditambahkan!"); bersihForm(); refreshTabel(); }
                else JOptionPane.showMessageDialog(this, "Gagal! ID Varian mungkin sudah digunakan.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Input tidak valid!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnUpdateStok.addActionListener(e -> {
            if (tblVarian.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Pilih varian terlebih dahulu!"); return; }
            try {
                int idVarian = Integer.parseInt(tfIdVarian.getText().trim());
                int stokBaru = Integer.parseInt(tfStok.getText().trim());
                if (dao.updateStokVarian(idVarian, stokBaru)) { JOptionPane.showMessageDialog(this, "Stok berhasil diperbarui!"); bersihForm(); refreshTabel(); }
                else JOptionPane.showMessageDialog(this, "Gagal update stok!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Input tidak valid!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnTutup.addActionListener(e -> dispose());
        refreshTabel();
    }

    private void bersihForm() {
        tfIdVarian.setText(""); tfIdVarian.setEditable(true);
        tfWarna.setText(""); tfUkuran.setText(""); tfStok.setText("");
        tblVarian.clearSelection();
    }

    private void refreshTabel() {
        tableModel.setRowCount(0);
        for (VarianProduk vp : dao.getVarianByProduk(skuProduk))
            tableModel.addRow(new Object[]{vp.getIdVarian(), vp.getWarna(), vp.getUkuran(), vp.getStok()});
    }
}
