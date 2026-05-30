package backend;

import dao.AnalisisDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * TopProdukPanel.java
 * Panel admin untuk menampilkan TOP 5 Produk Terlaris.
 *
 * Soal (a): Apa saja 5 produk dengan penjualan tertinggi?
 *
 * Teknik SQL yang digunakan:
 *   - VIEW (VW_TOP5_PRODUK_TERLARIS) yang sudah dibuat di SQL Server
 *   - Di dalam VIEW: JOIN (DETAIL_PESANAN, VARIAN_PRODUK, PRODUK, KATEGORI, PESANAN)
 *                    GROUP BY, SUM (agregasi), ORDER BY, TOP 5
 *   - Java hanya memanggil SELECT * FROM VW_TOP5_PRODUK_TERLARIS
 */
public class TopProdukPanel extends JPanel {

    private AnalisisDAO analisisDAO;
    private DefaultTableModel tableModel;
    private JTable tabelProduk;

    private static final NumberFormat fmt = NumberFormat.getInstance(new Locale("id", "ID"));

    public TopProdukPanel() {
        analisisDAO = new AnalisisDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ---- HEADER ----
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(23, 162, 184));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lblJudul = new JLabel("TOP 5 Produk Terlaris Zalora");
        lblJudul.setFont(new Font("Arial", Font.BOLD, 17));
        lblJudul.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Data diambil dari VIEW VW_TOP5_PRODUK_TERLARIS  |  Teknik: VIEW + JOIN + GROUP BY + SUM + TOP");
        lblSub.setFont(new Font("Arial", Font.ITALIC, 11));
        lblSub.setForeground(new Color(200, 240, 255));

        panelHeader.add(lblJudul, BorderLayout.NORTH);
        panelHeader.add(lblSub,   BorderLayout.SOUTH);
        add(panelHeader, BorderLayout.NORTH);

        // ---- TABEL ----
        String[] kolom = {
            "Rank", "SKU Produk", "Nama Produk", "Merek",
            "Kategori", "Total Terjual", "Total Pendapatan (Rp)", "Jml Transaksi"
        };
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelProduk = new JTable(tableModel);
        tabelProduk.setRowHeight(28);
        tabelProduk.setFont(new Font("Arial", Font.PLAIN, 13));
        tabelProduk.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tabelProduk.getTableHeader().setBackground(new Color(23, 162, 184));
        tabelProduk.getTableHeader().setForeground(Color.WHITE);
        tabelProduk.setSelectionBackground(new Color(173, 216, 230));

        // Set lebar kolom
        int[] lebar = {40, 90, 200, 110, 100, 100, 160, 110};
        for (int i = 0; i < lebar.length; i++) {
            tabelProduk.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }

        // Center alignment untuk kolom angka
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i : new int[]{0, 5, 7}) {
            tabelProduk.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Highlight ranking 1 dengan warna emas
        tabelProduk.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (row == 0)      c.setBackground(new Color(255, 215, 0, 80));   // Emas - rank 1
                    else if (row == 1) c.setBackground(new Color(192, 192, 192, 60)); // Perak - rank 2
                    else if (row == 2) c.setBackground(new Color(205, 127, 50, 60));  // Perunggu - rank 3
                    else               c.setBackground(Color.WHITE);
                }
                if (column == 0 || column == 5 || column == 7) {
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        });

        add(new JScrollPane(tabelProduk), BorderLayout.CENTER);

        // ---- PANEL BAWAH: tombol refresh + keterangan teknis ----
        JPanel panelBawah = new JPanel(new BorderLayout(10, 5));
        panelBawah.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton btnRefresh = new JButton("↺  Refresh Data");
        btnRefresh.setBackground(new Color(23, 162, 184));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 12));
        btnRefresh.setFocusPainted(false);

        JLabel lblKet = new JLabel(
            "<html><i>* Pesanan berstatus 'Dibatalkan' tidak dihitung.  " +
            "Data diambil via VIEW SQL Server — JOIN 5 tabel + GROUP BY + SUM.</i></html>");
        lblKet.setFont(new Font("Arial", Font.PLAIN, 11));
        lblKet.setForeground(Color.GRAY);

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBtn.add(btnRefresh);

        panelBawah.add(lblKet,   BorderLayout.CENTER);
        panelBawah.add(panelBtn, BorderLayout.EAST);
        add(panelBawah, BorderLayout.SOUTH);

        // ---- LOAD DATA AWAL ----
        loadData();

        btnRefresh.addActionListener(e -> loadData());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Object[]> list = analisisDAO.getTop5ProdukTerlaris();

        if (list.isEmpty()) {
            tableModel.addRow(new Object[]{
                "-", "-", "Belum ada data transaksi", "-", "-", 0, "Rp 0", 0
            });
            return;
        }

        for (Object[] row : list) {
            // row: [Rank, SKU, Nama, Merek, Kategori, Total_Terjual, Total_Pendapatan, Jml_Transaksi]
            tableModel.addRow(new Object[]{
                "#" + row[0],
                row[1],
                row[2],
                row[3],
                row[4],
                row[5] + " pcs",
                "Rp " + fmt.format(row[6]),
                row[7] + " pesanan"
            });
        }
    }
} // AKHIR DARI FILE TopProdukPanel.java
