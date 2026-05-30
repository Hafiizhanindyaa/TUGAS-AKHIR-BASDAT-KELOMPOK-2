package frontend;

import config.SessionManager;
import dao.UlasanDAO;
import model.Ulasan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class UlasanPanel extends JPanel {

    private UlasanDAO ulasanDAO;

    private JTable tblUlasan;
    private DefaultTableModel mdlUlasan;

    private JComboBox<String> cbProduk;
    private JSlider sliderRating;
    private JLabel lblBintang;
    private JButton btnTambah, btnUpdate, btnHapus, btnBersih, btnRefresh;

    private List<Object[]> listProdukDibeli;
    private int idUlasanTerpilih = -1;

    public UlasanPanel() {
        this.ulasanDAO = new UlasanDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        buatUI();
        muatData();
    }

    private void buatUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Ulasan Produk Saya");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel lblSubtitle = new JLabel("Hanya produk dengan pesanan berstatus 'Selesai' yang dapat diulas.");
        lblSubtitle.setFont(new Font("Arial", Font.ITALIC, 11));
        lblSubtitle.setForeground(Color.GRAY);
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"ID Ulasan", "Nama Produk", "SKU", "Rating", "Tanggal"};
        mdlUlasan = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblUlasan = new JTable(mdlUlasan);
        tblUlasan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUlasan.setRowHeight(28);
        tblUlasan.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblUlasan.getColumnModel().getColumn(1).setPreferredWidth(220);
        tblUlasan.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblUlasan.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblUlasan.getColumnModel().getColumn(4).setPreferredWidth(110);

        tblUlasan.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (value instanceof Integer) {
                    int r = (Integer) value;
                    setText("★".repeat(r) + "☆".repeat(5 - r) + " (" + r + ")");
                    setForeground(new Color(255, 165, 0));
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblUlasan);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Daftar Ulasan Saya"));
        add(scrollPane, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Ulasan"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Produk yang Diulas:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cbProduk = new JComboBox<>();
        formPanel.add(cbProduk, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Rating (1-5):"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JPanel ratingPanel = new JPanel(new BorderLayout(8, 0));
        sliderRating = new JSlider(1, 5, 3);
        sliderRating.setMajorTickSpacing(1);
        sliderRating.setPaintTicks(true);
        sliderRating.setPaintLabels(true);
        sliderRating.setSnapToTicks(true);
        lblBintang = new JLabel(getBintangText(3));
        lblBintang.setFont(new Font("Arial", Font.BOLD, 14));
        lblBintang.setForeground(new Color(255, 165, 0));
        ratingPanel.add(sliderRating, BorderLayout.CENTER);
        ratingPanel.add(lblBintang, BorderLayout.EAST);
        formPanel.add(ratingPanel, gbc);

        sliderRating.addChangeListener(e -> lblBintang.setText(getBintangText(sliderRating.getValue())));

                gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnTambah = new JButton("+ Tambah Ulasan");
        btnUpdate = new JButton("Edit Rating");
        btnHapus  = new JButton("Hapus Ulasan");
        btnBersih = new JButton("Bersihkan");
        btnRefresh = new JButton("Refresh");
        btnTambah.setBackground(new Color(40, 167, 69));  btnTambah.setForeground(Color.WHITE);
        btnHapus.setBackground(new Color(220, 53, 69));   btnHapus.setForeground(Color.WHITE);
        btnUpdate.setBackground(new Color(255, 193, 7));
        btnPanel.add(btnTambah); btnPanel.add(btnUpdate);
        btnPanel.add(btnHapus);  btnPanel.add(btnBersih); btnPanel.add(btnRefresh);
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.SOUTH);


        tblUlasan.getSelectionModel().addListSelectionListener(e -> {
            int row = tblUlasan.getSelectedRow();
            if (row >= 0) {
                idUlasanTerpilih = Integer.parseInt(tblUlasan.getValueAt(row, 0).toString());
                String skuTerpilih = tblUlasan.getValueAt(row, 2).toString();
                int ratingTerpilih = Integer.parseInt(tblUlasan.getValueAt(row, 3).toString());

                                for (int i = 0; i < cbProduk.getItemCount(); i++) {
                    if (cbProduk.getItemAt(i).startsWith(skuTerpilih)) {
                        cbProduk.setSelectedIndex(i);
                        break;
                    }
                }
                sliderRating.setValue(ratingTerpilih);
            }
        });

        btnTambah.addActionListener(e -> tambahUlasan());
        btnUpdate.addActionListener(e -> updateUlasan());
        btnHapus.addActionListener(e -> hapusUlasan());
        btnBersih.addActionListener(e -> bersihForm());
        btnRefresh.addActionListener(e -> muatData());
    }

    private void tambahUlasan() {
        if (cbProduk.getSelectedIndex() < 0 || listProdukDibeli == null || listProdukDibeli.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada produk yang bisa diulas saat ini.\nPastikan ada pesanan dengan status 'Selesai'.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String sku = listProdukDibeli.get(cbProduk.getSelectedIndex())[0].toString();
        int rating = sliderRating.getValue();
        int nextId = ulasanDAO.getNextIdUlasan();

        Ulasan u = new Ulasan(nextId, SessionManager.getIdPelanggan(), sku, null, rating);
        if (ulasanDAO.insertUlasan(u)) {
            JOptionPane.showMessageDialog(this, "Ulasan berhasil ditambahkan! " + getBintangText(rating));
            bersihForm(); muatData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menambah ulasan. Mungkin Anda sudah mengulas produk ini.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUlasan() {
        if (idUlasanTerpilih < 0) {
            JOptionPane.showMessageDialog(this, "Pilih ulasan yang ingin diubah dari tabel!");
            return;
        }
        int ratingBaru = sliderRating.getValue();
        Ulasan u = new Ulasan(idUlasanTerpilih, SessionManager.getIdPelanggan(), "", null, ratingBaru);
        if (ulasanDAO.updateUlasan(u)) {
            JOptionPane.showMessageDialog(this, "Rating ulasan berhasil diperbarui! " + getBintangText(ratingBaru));
            bersihForm(); muatData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui ulasan!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusUlasan() {
        if (idUlasanTerpilih < 0) {
            JOptionPane.showMessageDialog(this, "Pilih ulasan yang ingin dihapus dari tabel!");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus ulasan ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (ulasanDAO.deleteUlasan(idUlasanTerpilih, SessionManager.getIdPelanggan())) {
                JOptionPane.showMessageDialog(this, "Ulasan berhasil dihapus!");
                bersihForm(); muatData();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus ulasan!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void bersihForm() {
        idUlasanTerpilih = -1;
        sliderRating.setValue(3);
        if (cbProduk.getItemCount() > 0) cbProduk.setSelectedIndex(0);
        tblUlasan.clearSelection();
    }

    public void muatData() {
        mdlUlasan.setRowCount(0);
        List<Object[]> ulasanList = ulasanDAO.getUlasanLengkapByPelanggan(SessionManager.getIdPelanggan());
        for (Object[] row : ulasanList) {
            mdlUlasan.addRow(row);
        }

        cbProduk.removeAllItems();
        listProdukDibeli = ulasanDAO.getProdukPernahDibeli(SessionManager.getIdPelanggan());
        if (listProdukDibeli.isEmpty()) {
            cbProduk.addItem("-- Belum ada pesanan selesai --");
        } else {
            for (Object[] p : listProdukDibeli) {
                cbProduk.addItem(p[0].toString() + " - " + p[1].toString());
            }
        }
    }

    private String getBintangText(int rating) {
        return "★".repeat(rating) + "☆".repeat(5 - rating) + " (" + rating + "/5)";
    }
}