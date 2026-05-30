package backend;

import dao.InventoryDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import config.DBConnection;

public class EditProductDialog extends JDialog {
    private JTextField tfNama, tfMerek, tfHarga, tfDeskripsi;
    private JButton btnSimpan, btnBatal;
    private InventoryManagementPanel parentPanel;
    private String skuProduk;

    public EditProductDialog(Frame parent, InventoryManagementPanel panel, String sku, String nama, String merek, double harga) {
        super(parent, "Edit Produk: " + sku, true);
        this.parentPanel = panel;
        this.skuProduk = sku;
        initUI(nama, merek, harga);
    }

    private void initUI(String nama, String merek, double harga) {
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        formPanel.add(new JLabel("SKU Produk (tidak bisa diubah):"));
        JTextField tfSku = new JTextField(skuProduk);
        tfSku.setEditable(false);
        tfSku.setBackground(Color.LIGHT_GRAY);
        formPanel.add(tfSku);

        formPanel.add(new JLabel("Nama Produk:"));
        tfNama = new JTextField(nama); formPanel.add(tfNama);

        formPanel.add(new JLabel("Merek:"));
        tfMerek = new JTextField(merek); formPanel.add(tfMerek);

        formPanel.add(new JLabel("Harga Jual:"));
        tfHarga = new JTextField(String.valueOf(harga)); formPanel.add(tfHarga);

        add(formPanel, BorderLayout.CENTER);

        JPanel panelButtons = new JPanel();
        btnSimpan = new JButton("Simpan Perubahan");
        btnBatal = new JButton("Batal");
        btnSimpan.setBackground(new Color(40, 167, 69)); btnSimpan.setForeground(Color.WHITE);
        panelButtons.add(btnSimpan); panelButtons.add(btnBatal);
        add(panelButtons, BorderLayout.SOUTH);

        btnSimpan.addActionListener(e -> {
            try {
                String namaBaru = tfNama.getText().trim();
                String merekBaru = tfMerek.getText().trim();
                double hargaBaru = Double.parseDouble(tfHarga.getText().trim());

                if (namaBaru.isEmpty() || merekBaru.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nama dan Merek tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String sql = "UPDATE PRODUK SET Nama_Produk=?, Merek=?, Harga_Jual=? WHERE SKU_Produk=?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, namaBaru);
                    ps.setString(2, merekBaru);
                    ps.setDouble(3, hargaBaru);
                    ps.setString(4, skuProduk);
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, "Data produk berhasil diperbarui!");
                        parentPanel.refreshTabelProduk();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal memperbarui data!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Input tidak valid!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBatal.addActionListener(e -> dispose());
    }
}
