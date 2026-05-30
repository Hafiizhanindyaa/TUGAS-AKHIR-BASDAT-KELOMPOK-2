package frontend;

import config.SessionManager;
import dao.InventoryDAO;
import model.Produk;
import model.VarianProduk;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CartPanel extends JPanel {
    private JTable tblKeranjang;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;

    // Data keranjang disimpan sebagai list item in-memory
    private List<Object[]> keranjangItems = new ArrayList<>(); // {idVarian, namaProduk, warna, ukuran, harga, qty, subtotal}
    private InventoryDAO inventoryDAO;
    private CustomerDashboardFrame parentFrame;

    public CartPanel(CustomerDashboardFrame frame) {
        this.parentFrame = frame;
        this.inventoryDAO = new InventoryDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Keranjang Belanja Saya");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        // Tabel keranjang
        String[] cols = {"ID Varian", "Nama Produk", "Warna", "Ukuran", "Harga/pcs", "Qty", "Subtotal"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblKeranjang = new JTable(tableModel);
        tblKeranjang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tblKeranjang), BorderLayout.CENTER);

        // Panel bawah
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        lblTotal = new JLabel("Total: Rp 0", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnHapusItem = new JButton("Hapus Item Terpilih");
        JButton btnKosongkan = new JButton("Kosongkan Keranjang");
        JButton btnCheckout = new JButton("Lanjut ke Checkout >");
        btnHapusItem.setBackground(new Color(220, 53, 69)); btnHapusItem.setForeground(Color.WHITE);
        btnCheckout.setBackground(new Color(40, 167, 69)); btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setFont(new Font("Arial", Font.BOLD, 13));

        btnPanel.add(btnHapusItem); btnPanel.add(btnKosongkan); btnPanel.add(btnCheckout);
        bottomPanel.add(lblTotal, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        btnHapusItem.addActionListener(e -> {
            int row = tblKeranjang.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih item yang ingin dihapus!"); return; }
            keranjangItems.remove(row);
            refreshTabel();
        });

        btnKosongkan.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Kosongkan semua isi keranjang?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                keranjangItems.clear();
                refreshTabel();
            }
        });

        btnCheckout.addActionListener(e -> {
            if (keranjangItems.isEmpty()) { JOptionPane.showMessageDialog(this, "Keranjang masih kosong!"); return; }
            parentFrame.bukaCheckout(keranjangItems);
        });
    }

    public void tambahKeKeranjang(int idVarian, String namaProduk, String warna, String ukuran, double harga) {
        // Cek apakah varian sudah ada, kalau iya tambah qty saja
        for (Object[] item : keranjangItems) {
            if ((int)item[0] == idVarian) {
                int qtyLama = (int)item[5];
                item[5] = qtyLama + 1;
                item[6] = harga * (qtyLama + 1);
                refreshTabel();
                JOptionPane.showMessageDialog(this, "Qty " + namaProduk + " ditambahkan menjadi " + (qtyLama + 1));
                return;
            }
        }
        keranjangItems.add(new Object[]{idVarian, namaProduk, warna, ukuran, harga, 1, harga});
        refreshTabel();
    }

    public List<Object[]> getKeranjangItems() { return keranjangItems; }

    public void refreshTabel() {
        tableModel.setRowCount(0);
        double total = 0;
        for (Object[] item : keranjangItems) {
            tableModel.addRow(item);
            total += (double)item[6];
        }
        lblTotal.setText(String.format("Total Belanja: Rp %,.0f", total));
    }
}
