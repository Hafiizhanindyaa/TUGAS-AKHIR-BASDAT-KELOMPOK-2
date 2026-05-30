package backend;

import dao.InventoryDAO;
import model.*;

import javax.swing.*;
import java.awt.*;

public class AddProductDialog extends JDialog {
    private JTextField tfSku, tfNama, tfMerek, tfHarga, tfKatId, tfParam1, tfParam2;
    private JComboBox<String> cbType;
    private JLabel lblParam1, lblParam2;
    private JButton btnSimpan, btnBatal;
    private InventoryDAO inventoryDAO;
    private InventoryManagementPanel parentPanel;

    public AddProductDialog(Frame parent, InventoryManagementPanel panel) {
        super(parent, "Tambah Produk Subtype Baru", true);
        this.parentPanel = panel;
        this.inventoryDAO = new InventoryDAO();
        initUI();
    }

    private void initUI() {
        setSize(420, 450);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel panelForm = new JPanel(new GridLayout(8, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        panelForm.add(new JLabel("SKU Produk:"));
        tfSku = new JTextField();
        panelForm.add(tfSku);

        panelForm.add(new JLabel("Nama Produk:"));
        tfNama = new JTextField();
        panelForm.add(tfNama);

        panelForm.add(new JLabel("Merek:"));
        tfMerek = new JTextField();
        panelForm.add(tfMerek);

        panelForm.add(new JLabel("Harga Jual:"));
        tfHarga = new JTextField();
        panelForm.add(tfHarga);

        panelForm.add(new JLabel("ID Kategori:"));
        tfKatId = new JTextField();
        panelForm.add(tfKatId);

        panelForm.add(new JLabel("Jenis Subtype:"));
        String[] types = {"PAKAIAN", "SEPATU", "AKSESORIS"};
        cbType = new JComboBox<>(types);
        panelForm.add(cbType);

        lblParam1 = new JLabel("Jenis Potongan:");
        tfParam1 = new JTextField();
        panelForm.add(lblParam1);
        panelForm.add(tfParam1);

        lblParam2 = new JLabel("Panduan Perawatan:");
        tfParam2 = new JTextField();
        panelForm.add(lblParam2);
        panelForm.add(tfParam2);

        add(panelForm, BorderLayout.CENTER);

        JPanel panelButtons = new JPanel();
        btnSimpan = new JButton("Simpan ke DB");
        btnBatal = new JButton("Batal");
        panelButtons.add(btnSimpan);
        panelButtons.add(btnBatal);
        add(panelButtons, BorderLayout.SOUTH);

        cbType.addActionListener(e -> {
            String selected = cbType.getSelectedItem().toString();
            if (selected.equals("PAKAIAN")) {
                lblParam1.setText("Jenis Potongan:");
                lblParam2.setText("Panduan Perawatan:");
            } else if (selected.equals("SEPATU")) {
                lblParam1.setText("Material Pembuat:");
                lblParam2.setText("Jenis Sepatu:");
            } else if (selected.equals("AKSESORIS")) {
                lblParam1.setText("Jenis Aksesoris:");
                lblParam2.setText("Material Pembuat:");
            }
        });

        btnSimpan.addActionListener(e -> {
            try {
                Produk p = new Produk();
                p.setSkuProduk(tfSku.getText().trim());
                p.setNamaProduk(tfNama.getText().trim());
                p.setDeskripsi("Deskripsi produk " + p.getNamaProduk());
                p.setMerek(tfMerek.getText().trim());
                p.setHargaJual(Double.parseDouble(tfHarga.getText().trim()));
                p.setIdKategori(Integer.parseInt(tfKatId.getText().trim()));

                String typeSelected = cbType.getSelectedItem().toString();
                Object detailSub = null;

                if (typeSelected.equals("PAKAIAN")) {
                detailSub = new Pakaian(
                    p.getSkuProduk(),
                    p.getNamaProduk(),
                    p.getDeskripsi(),
                    p.getMerek(),
                    p.getHargaJual(),
                    p.getIdKategori(),
                    tfParam1.getText().trim(),
                    tfParam2.getText().trim()
                );
                } else if (typeSelected.equals("SEPATU")) {
                    detailSub = new Sepatu(
                        p.getSkuProduk(),
                        p.getNamaProduk(),
                        p.getDeskripsi(),
                        p.getMerek(),
                        p.getHargaJual(),
                        p.getIdKategori(),
                        tfParam1.getText().trim(),
                        tfParam2.getText().trim()
                    );
                } else if (typeSelected.equals("AKSESORIS")) {
                    detailSub = new Aksesoris(
                        p.getSkuProduk(),
                        p.getNamaProduk(),
                        p.getDeskripsi(),
                        p.getMerek(),
                        p.getHargaJual(),
                        p.getIdKategori(),
                        tfParam1.getText().trim(),
                        tfParam2.getText().trim()
                    );
                }

                if (inventoryDAO.insertProdukLengkap(p, typeSelected, detailSub)) {
                    JOptionPane.showMessageDialog(this, "Data entitas baru berhasil tersimpan!");
                    parentPanel.refreshTabelProduk();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan data! Periksa integritas/SKU.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Format input data tidak valid!\n" + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBatal.addActionListener(e -> dispose());
    }
}