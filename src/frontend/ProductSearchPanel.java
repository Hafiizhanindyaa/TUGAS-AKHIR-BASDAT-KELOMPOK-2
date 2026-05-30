package frontend;

import dao.FrontendDAO;
import dao.InventoryDAO;
import model.VarianProduk;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductSearchPanel extends JPanel {
    private JTextField tfSearch, tfHargaMin, tfHargaMax;
    private JComboBox<String> cbTipe, cbKategori, cbSortBy;
    private JCheckBox chkSortAsc;
    private JButton btnSearch, btnReset;
    private JTable tableKatalog;
    private DefaultTableModel tableModel;
    private FrontendDAO frontendDAO;
    private InventoryDAO inventoryDAO;
    private CustomerDashboardFrame parentFrame;

    public ProductSearchPanel(CustomerDashboardFrame frame) {
        this.parentFrame  = frame;
        this.frontendDAO  = new FrontendDAO();
        this.inventoryDAO = new InventoryDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Katalog Belanja Zalora");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        JPanel panelFilter = new JPanel(new GridLayout(3, 4, 8, 6));
        panelFilter.setBorder(BorderFactory.createTitledBorder("Filter Pencarian"));

        panelFilter.add(new JLabel("Cari Nama/Merek/Deskripsi:"));
        tfSearch = new JTextField();
        panelFilter.add(tfSearch);

        panelFilter.add(new JLabel("Tipe Produk:"));
        cbTipe = new JComboBox<>(new String[]{"Semua", "PAKAIAN", "SEPATU", "AKSESORIS"});
        panelFilter.add(cbTipe);

        panelFilter.add(new JLabel("Harga Minimum (Rp):"));
        tfHargaMin = new JTextField();
        tfHargaMin.setToolTipText("Kosongkan jika tidak ingin filter harga minimum");
        panelFilter.add(tfHargaMin);

        panelFilter.add(new JLabel("Harga Maksimum (Rp):"));
        tfHargaMax = new JTextField();
        tfHargaMax.setToolTipText("Kosongkan jika tidak ingin filter harga maksimum");
        panelFilter.add(tfHargaMax);

        panelFilter.add(new JLabel("Kategori:"));
        cbKategori = new JComboBox<>();
        loadKategori();
        panelFilter.add(cbKategori);

        panelFilter.add(new JLabel("Urutkan Berdasarkan:"));
        cbSortBy = new JComboBox<>(new String[]{
            "Nama_Produk", "Harga_Jual", "Jumlah_Terjual", "Total_Stok"
        });
        panelFilter.add(cbSortBy);

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        chkSortAsc = new JCheckBox("Urutan Naik (ASC)", true);
        btnSearch  = new JButton("Cari Produk");
        btnReset   = new JButton("Reset Filter");
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Arial", Font.BOLD, 12));
        panelBtn.add(chkSortAsc);
        panelBtn.add(btnSearch);
        panelBtn.add(btnReset);

        JPanel panelTop = new JPanel(new BorderLayout(5, 5));
        panelTop.add(panelFilter, BorderLayout.CENTER);
        panelTop.add(panelBtn,   BorderLayout.SOUTH);

        String[] columns = {"SKU", "Nama Produk", "Merek", "Harga", "Kategori",
                            "Tipe", "Spesifikasi", "Stok", "Terjual"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tableKatalog = new JTable(tableModel);
        tableKatalog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableKatalog.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableKatalog.getColumnModel().getColumn(1).setPreferredWidth(150);
        tableKatalog.getColumnModel().getColumn(6).setPreferredWidth(150);

        JPanel panelCenter = new JPanel(new BorderLayout(10, 10));
        panelCenter.add(panelTop, BorderLayout.NORTH);
        panelCenter.add(new JScrollPane(tableKatalog), BorderLayout.CENTER);
        add(panelCenter, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnLihatVarian = new JButton("Lihat Varian & Stok");
        JButton btnAddCart     = new JButton("+ Masukkan ke Keranjang");
        btnAddCart.setBackground(new Color(40, 167, 69));
        btnAddCart.setForeground(Color.WHITE);
        btnAddCart.setFont(new Font("Arial", Font.BOLD, 12));
        panelBottom.add(btnLihatVarian);
        panelBottom.add(btnAddCart);
        add(panelBottom, BorderLayout.SOUTH);

        doSearch();

                btnSearch.addActionListener(e -> doSearch());
        tfSearch.addActionListener(e -> doSearch());

        btnReset.addActionListener(e -> {
            tfSearch.setText("");
            tfHargaMin.setText("");
            tfHargaMax.setText("");
            cbTipe.setSelectedIndex(0);
            cbKategori.setSelectedIndex(0);
            cbSortBy.setSelectedIndex(0);
            chkSortAsc.setSelected(true);
            doSearch();
        });

        btnLihatVarian.addActionListener(e -> {
            int row = tableKatalog.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih produk terlebih dahulu!"); return; }
            String sku  = tableKatalog.getValueAt(row, 0).toString();
            String nama = tableKatalog.getValueAt(row, 1).toString();
            showVarianDialog(sku, nama, false);
        });

        btnAddCart.addActionListener(e -> {
            int row = tableKatalog.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih produk terlebih dahulu!"); return; }
            int stok = Integer.parseInt(tableKatalog.getValueAt(row, 7).toString());
            if (stok <= 0) { JOptionPane.showMessageDialog(this, "Produk ini sudah habis stoknya!"); return; }
            String sku  = tableKatalog.getValueAt(row, 0).toString();
            String nama = tableKatalog.getValueAt(row, 1).toString();
            showVarianDialog(sku, nama, true);
        });
    }


    private void doSearch() {
        tableModel.setRowCount(0);

        String keyword    = tfSearch.getText().trim();
        String tipe       = cbTipe.getSelectedItem().toString();
        String kategori   = cbKategori.getSelectedItem().toString();
        String sortBy     = cbSortBy.getSelectedItem().toString();
        boolean sortAsc   = chkSortAsc.isSelected();

        Double hargaMin = null, hargaMax = null;
        try {
            if (!tfHargaMin.getText().trim().isEmpty())
                hargaMin = Double.parseDouble(tfHargaMin.getText().trim());
            if (!tfHargaMax.getText().trim().isEmpty())
                hargaMax = Double.parseDouble(tfHargaMax.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Format harga tidak valid! Masukkan angka saja.");
            return;
        }

        List<Object[]> results = frontendDAO.searchProdukAdvanced(
            keyword, tipe, hargaMin, hargaMax, kategori, sortBy, sortAsc);

        for (Object[] row : results) {
            tableModel.addRow(new Object[]{
                row[0], row[1], row[2],
                String.format("Rp %,.0f", (double) row[3]),
                row[4], row[5], row[6], row[7], row[8]
            });
        }

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Tidak ada produk yang sesuai dengan filter pencarian.",
                "Hasil Pencarian", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadKategori() {
        List<String> kategoriList = frontendDAO.getAllKategori();
        for (String k : kategoriList) cbKategori.addItem(k);
    }

    private void showVarianDialog(String sku, String namaProduk, boolean modeAddToCart) {
        List<VarianProduk> varianList = inventoryDAO.getVarianByProduk(sku);
        if (varianList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Produk ini belum memiliki varian/stok!");
            return;
        }

        String[] cols = {"ID Varian", "Warna", "Ukuran", "Stok"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(mdl);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (VarianProduk vp : varianList)
            mdl.addRow(new Object[]{vp.getIdVarian(), vp.getWarna(), vp.getUkuran(), vp.getStok()});

        JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(this) instanceof Frame
                ? (Frame) SwingUtilities.getWindowAncestor(this) : null,
            "Varian Produk: " + namaProduk, true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.add(new JScrollPane(tbl), BorderLayout.CENTER);

        if (modeAddToCart) {
            JButton btnPilih = new JButton("Pilih Varian Ini -> Tambah ke Keranjang");
            btnPilih.setBackground(new Color(40, 167, 69));
            btnPilih.setForeground(Color.WHITE);
            btnPilih.addActionListener(ev -> {
                int row = tbl.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(dialog, "Pilih salah satu varian!"); return; }
                int stok = (int) mdl.getValueAt(row, 3);
                if (stok <= 0) { JOptionPane.showMessageDialog(dialog, "Stok habis!"); return; }
                int    idVarian = (int)    mdl.getValueAt(row, 0);
                String warna    = mdl.getValueAt(row, 1).toString();
                String ukuran   = mdl.getValueAt(row, 2).toString();
                                int selectedRow = tableKatalog.getSelectedRow();
                                String hargaStr = tableKatalog.getValueAt(selectedRow, 3).toString()
                .replace("Rp ", "").replace(".", "").replace(",", "").trim();
                double harga = Double.parseDouble(hargaStr);
                parentFrame.getCartPanel().tambahKeKeranjang(idVarian, namaProduk, warna, ukuran, harga);
                JOptionPane.showMessageDialog(dialog,
                    namaProduk + " (" + warna + "/" + ukuran + ") ditambahkan ke keranjang!");
                dialog.dispose();
            });
            dialog.add(btnPilih, BorderLayout.SOUTH);
        } else {
            JButton btnTutup = new JButton("Tutup");
            btnTutup.addActionListener(ev -> dialog.dispose());
            dialog.add(btnTutup, BorderLayout.SOUTH);
        }

        dialog.setVisible(true);
    }
}
