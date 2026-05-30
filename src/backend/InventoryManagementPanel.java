package backend;

import dao.InventoryDAO;
import model.Produk;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryManagementPanel extends JPanel {
    private JTable tableBarang;
    private DefaultTableModel tableModel;
    private InventoryDAO inventoryDAO;

    public InventoryManagementPanel() {
        inventoryDAO = new InventoryDAO();
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Manajemen Data Gudang & Inventory (Zalora)");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"SKU", "Nama Produk", "Merek", "Harga Jual", "Tipe Subtype"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableBarang = new JTable(tableModel);
        tableBarang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tableBarang), BorderLayout.CENTER);

        JPanel panelAksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnTambah = new JButton("+ Tambah Produk Baru");
        JButton btnEdit = new JButton("Edit Produk Terpilih");
        JButton btnHapus = new JButton("Hapus Produk Terpilih");
        JButton btnVarian = new JButton("Kelola Varian / Stok");

        btnTambah.setBackground(new Color(40, 167, 69)); btnTambah.setForeground(Color.WHITE);
        btnHapus.setBackground(new Color(220, 53, 69)); btnHapus.setForeground(Color.WHITE);
        btnEdit.setBackground(new Color(255, 193, 7));
        btnVarian.setBackground(new Color(23, 162, 184)); btnVarian.setForeground(Color.WHITE);

        panelAksi.add(btnTambah);
        panelAksi.add(btnEdit);
        panelAksi.add(btnHapus);
        panelAksi.add(btnVarian);
        add(panelAksi, BorderLayout.SOUTH);

        refreshTabelProduk();

        btnTambah.addActionListener(e -> {
            Window ancestor = SwingUtilities.getWindowAncestor(this);
            if (ancestor instanceof Frame) {
                AddProductDialog dialog = new AddProductDialog((Frame) ancestor, this);
                dialog.setVisible(true);
            }
        });

        btnEdit.addActionListener(e -> {
            int selectedRow = tableBarang.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Pilih produk yang ingin diedit!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sku = tableBarang.getValueAt(selectedRow, 0).toString();
            String nama = tableBarang.getValueAt(selectedRow, 1).toString();
            String merek = tableBarang.getValueAt(selectedRow, 2).toString();
            double harga = Double.parseDouble(tableBarang.getValueAt(selectedRow, 3).toString());

            Window ancestor = SwingUtilities.getWindowAncestor(this);
            if (ancestor instanceof Frame) {
                EditProductDialog dialog = new EditProductDialog((Frame) ancestor, this, sku, nama, merek, harga);
                dialog.setVisible(true);
            }
        });

        btnHapus.addActionListener(e -> {
            int selectedRow = tableBarang.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Pilih salah satu baris produk yang ingin dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sku = tableBarang.getValueAt(selectedRow, 0).toString();
            int konfirm = JOptionPane.showConfirmDialog(this,
                "Hapus Produk SKU: " + sku + "?\n(Semua sub-tabel & varian stok terkait akan ikut terhapus otomatis)",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (konfirm == JOptionPane.YES_OPTION) {
                if (inventoryDAO.deleteProduk(sku)) {
                    JOptionPane.showMessageDialog(this, "Produk berhasil didelete dari basis data!");
                    refreshTabelProduk();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus data!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnVarian.addActionListener(e -> {
            int selectedRow = tableBarang.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Pilih produk terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sku = tableBarang.getValueAt(selectedRow, 0).toString();
            String namaProduk = tableBarang.getValueAt(selectedRow, 1).toString();
            Window ancestor = SwingUtilities.getWindowAncestor(this);
            if (ancestor instanceof Frame) {
                VarianManagementDialog dialog = new VarianManagementDialog((Frame) ancestor, sku, namaProduk);
                dialog.setVisible(true);
            }
        });
    }

    public void refreshTabelProduk() {
        tableModel.setRowCount(0);
        List<Produk> list = inventoryDAO.searchProdukKatalog("", null);
        for (Produk p : list) {
            String tipeSub = "Standar";
            if (p instanceof model.Pakaian) tipeSub = "Pakaian";
            else if (p instanceof model.Sepatu) tipeSub = "Sepatu";
            else if (p instanceof model.Aksesoris) tipeSub = "Aksesoris";
            tableModel.addRow(new Object[]{p.getSkuProduk(), p.getNamaProduk(), p.getMerek(), p.getHargaJual(), tipeSub});
        }
    }
}
