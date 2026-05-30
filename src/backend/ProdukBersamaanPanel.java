package backend;

import dao.AnalisisDAO;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * ProdukBersamaanPanel.java
 * Panel admin untuk menampilkan 3 produk yang paling sering dibeli
 * bersamaan dengan produk tertentu yang dipilih admin.
 *
 * Soal (c): Apa 3 barang yang paling banyak dibeli berbarengan
 *           dengan pembelian suatu produk tertentu?
 *
 * Teknik SQL yang digunakan:
 *   - SELF-JOIN tabel DETAIL_PESANAN
 *     (dp1 = item acuan, dp2 = item lain di pesanan yang sama)
 *   - Subquery tersirat: dp1.ID_Pesanan = dp2.ID_Pesanan (JOIN kondisi = subquery)
 *   - GROUP BY produk pasangan, COUNT frekuensi, TOP 3
 *   - PreparedStatement dengan parameter SKU produk acuan
 */
public class ProdukBersamaanPanel extends JPanel {

    private AnalisisDAO analisisDAO;
    private DefaultTableModel tableModel;
    private JTable tabelHasil;

    // Komponen pilihan produk acuan
    private JComboBox<String> cbProdukAcuan;
    private List<Object[]> listProduk; // cache [SKU, Nama]
    private JLabel lblProdukAcuanInfo;
    private JLabel lblHasilInfo;

    public ProdukBersamaanPanel() {
        analisisDAO = new AnalisisDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ---- HEADER ----
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(111, 66, 193)); // Ungu
        panelHeader.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lblJudul = new JLabel("3 Produk Paling Sering Dibeli Bersamaan");
        lblJudul.setFont(new Font("Arial", Font.BOLD, 17));
        lblJudul.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(
            "Teknik SQL: Self-Join DETAIL_PESANAN + Subquery + GROUP BY + COUNT + TOP 3  |  PreparedStatement");
        lblSub.setFont(new Font("Arial", Font.ITALIC, 11));
        lblSub.setForeground(new Color(220, 200, 255));

        panelHeader.add(lblJudul, BorderLayout.NORTH);
        panelHeader.add(lblSub,   BorderLayout.SOUTH);
        add(panelHeader, BorderLayout.NORTH);

        // ---- PANEL PILIH PRODUK ACUAN ----
        JPanel panelPilih = new JPanel(new BorderLayout(10, 8));
        panelPilih.setBorder(BorderFactory.createTitledBorder("Pilih Produk Acuan"));
        panelPilih.setBackground(new Color(248, 245, 255));

        JPanel panelForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        panelForm.setOpaque(false);

        panelForm.add(new JLabel("Produk:"));
        cbProdukAcuan = new JComboBox<>();
        cbProdukAcuan.setPreferredSize(new Dimension(320, 30));
        panelForm.add(cbProdukAcuan);

        JButton btnCari = new JButton("Cari Produk Bersamaan");
        btnCari.setBackground(new Color(111, 66, 193));
        btnCari.setForeground(Color.WHITE);
        btnCari.setFont(new Font("Arial", Font.BOLD, 12));
        btnCari.setFocusPainted(false);
        panelForm.add(btnCari);

        lblProdukAcuanInfo = new JLabel(
            "<html><i>Pilih produk dari dropdown, lalu klik 'Cari'. " +
            "Sistem akan mencari 3 produk yang paling sering dibeli dalam 1 pesanan yang sama.</i></html>");
        lblProdukAcuanInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        lblProdukAcuanInfo.setForeground(new Color(90, 70, 130));
        lblProdukAcuanInfo.setBorder(BorderFactory.createEmptyBorder(0, 12, 6, 12));

        panelPilih.add(panelForm,          BorderLayout.NORTH);
        panelPilih.add(lblProdukAcuanInfo, BorderLayout.SOUTH);
        add(panelPilih, BorderLayout.NORTH);

        // ---- TABEL HASIL ----
        String[] kolom = {"Rank", "SKU Produk", "Nama Produk", "Merek", "Frekuensi Bersamaan"};
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelHasil = new JTable(tableModel);
        tabelHasil.setRowHeight(30);
        tabelHasil.setFont(new Font("Arial", Font.PLAIN, 13));
        tabelHasil.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tabelHasil.getTableHeader().setBackground(new Color(111, 66, 193));
        tabelHasil.getTableHeader().setForeground(Color.WHITE);
        tabelHasil.setSelectionBackground(new Color(200, 185, 255));

        int[] lebar = {50, 100, 240, 130, 140};
        for (int i = 0; i < lebar.length; i++) {
            tabelHasil.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }

        // Highlight ranking dengan warna medali
        tabelHasil.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if      (row == 0) c.setBackground(new Color(255, 215, 0, 100));
                    else if (row == 1) c.setBackground(new Color(192, 192, 192, 70));
                    else if (row == 2) c.setBackground(new Color(205, 127, 50, 70));
                    else               c.setBackground(Color.WHITE);
                }
                if (column == 0 || column == 4) setHorizontalAlignment(JLabel.CENTER);
                else                             setHorizontalAlignment(JLabel.LEFT);
                return c;
            }
        });

        // Bungkus tabel + label info hasil dalam panel tengah
        JPanel panelTengah = new JPanel(new BorderLayout(5, 5));
        lblHasilInfo = new JLabel("← Pilih produk dan klik 'Cari' untuk melihat hasil.");
        lblHasilInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        lblHasilInfo.setForeground(new Color(111, 66, 193));
        lblHasilInfo.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));

        panelTengah.add(lblHasilInfo,              BorderLayout.NORTH);
        panelTengah.add(new JScrollPane(tabelHasil), BorderLayout.CENTER);
        add(panelTengah, BorderLayout.CENTER);

        // ---- PANEL BAWAH: keterangan teknis ----
        JPanel panelBawah = new JPanel(new BorderLayout());
        panelBawah.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JLabel lblKet = new JLabel(
            "<html><i>* Query menggunakan Self-Join DETAIL_PESANAN: " +
            "mencari item lain (dp2) yang ada di pesanan yang sama dengan item acuan (dp1). " +
            "Pesanan 'Dibatalkan' tidak dihitung.</i></html>");
        lblKet.setFont(new Font("Arial", Font.PLAIN, 11));
        lblKet.setForeground(Color.GRAY);

        JButton btnRefreshProduk = new JButton("↺  Refresh Daftar Produk");
        btnRefreshProduk.setFocusPainted(false);
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBtn.add(btnRefreshProduk);

        panelBawah.add(lblKet,   BorderLayout.CENTER);
        panelBawah.add(panelBtn, BorderLayout.EAST);
        add(panelBawah, BorderLayout.SOUTH);

        // ---- LOAD DAFTAR PRODUK ----
        loadDaftarProduk();

        // ---- AKSI ----
        btnCari.addActionListener(e -> cariProdukBersamaan());
        btnRefreshProduk.addActionListener(e -> loadDaftarProduk());

        // Enter di combo juga trigger
        cbProdukAcuan.addActionListener(null);
    }

    /** Mengisi dropdown produk dari database. */
    private void loadDaftarProduk() {
        cbProdukAcuan.removeAllItems();
        listProduk = analisisDAO.getAllProdukUntukDropdown();
        if (listProduk.isEmpty()) {
            cbProdukAcuan.addItem("-- Belum ada produk --");
        } else {
            cbProdukAcuan.addItem("-- Pilih Produk Acuan --");
            for (Object[] p : listProduk) {
                // Tampilkan: "NAMA_PRODUK  [SKU]"
                cbProdukAcuan.addItem(p[1] + "  [" + p[0] + "]");
            }
        }
        tableModel.setRowCount(0);
        lblHasilInfo.setText("← Pilih produk dan klik 'Cari' untuk melihat hasil.");
    }

    /** Mengambil dan menampilkan 3 produk bersamaan berdasarkan produk yang dipilih. */
    private void cariProdukBersamaan() {
        int idx = cbProdukAcuan.getSelectedIndex();
        if (listProduk == null || listProduk.isEmpty() || idx <= 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih produk acuan terlebih dahulu dari dropdown.",
                "Produk Belum Dipilih", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object[] produkDipilih = listProduk.get(idx - 1); // -1 karena index 0 = placeholder
        String skuAcuan  = (String) produkDipilih[0];
        String namaAcuan = (String) produkDipilih[1];

        // Reset tabel
        tableModel.setRowCount(0);
        lblHasilInfo.setText( "Menampilkan 3 produk yang paling sering dibeli bersamaan dengan: " + namaAcuan );
        lblHasilInfo.setForeground(new Color(111, 66, 193));

        List<Object[]> list = analisisDAO.get3ProdukSeringDibeliBareng(skuAcuan);

        if (list.isEmpty()) {
            tableModel.addRow(new Object[]{
                "-", "-",
                "Tidak ada data — produk ini belum pernah dibeli bersamaan produk lain",
                "-", 0
            });
            lblHasilInfo.setText("Tidak ada hasil untuk " + namaAcuan + " — " + "belum ada pesanan yang memuat produk ini bersama produk lain.");
            lblHasilInfo.setForeground(Color.RED);
            return;
        }

        for (Object[] row : list) {
            // row: [Rank, SKU, Nama, Merek, Frekuensi]
            tableModel.addRow(new Object[]{
                "#" + row[0],
                row[1],
                row[2],
                row[3],
                row[4] + "x ditemukan bersama"
            });
        }
    }
} // AKHIR DARI FILE ProdukBersamaanPanel.java
