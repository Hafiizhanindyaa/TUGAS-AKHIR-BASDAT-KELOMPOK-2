package frontend;

import config.SessionManager;
import dao.FrontendDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderHistoryPanel extends JPanel {
    private FrontendDAO frontendDAO;
    private JTable tblPesanan, tblDetail;
    private DefaultTableModel mdlPesanan, mdlDetail;
    private JComboBox<String> cbTahun, cbBulan, cbStatus;
    private JButton btnFilter, btnReset;

    public OrderHistoryPanel() {
        frontendDAO = new FrontendDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Riwayat Pesanan Saya");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFilter.setBorder(BorderFactory.createTitledBorder("Filter Riwayat Pesanan"));

        panelFilter.add(new JLabel("Tahun:"));
        cbTahun = new JComboBox<>(new String[]{"Semua", "2023", "2024", "2025", "2026"});
        panelFilter.add(cbTahun);

        panelFilter.add(new JLabel("Bulan:"));
        cbBulan = new JComboBox<>(new String[]{
            "Semua", "1-Jan", "2-Feb", "3-Mar", "4-Apr", "5-Mei", "6-Jun",
            "7-Jul", "8-Agu", "9-Sep", "10-Okt", "11-Nov", "12-Des"
        });
        panelFilter.add(cbBulan);

        panelFilter.add(new JLabel("Status:"));
        cbStatus = new JComboBox<>(new String[]{
            "Semua", "Diproses", "Dikirim", "Selesai", "Dibatalkan"
        });
        panelFilter.add(cbStatus);

        btnFilter = new JButton("Terapkan Filter");
        btnFilter.setBackground(new Color(0, 123, 255));
        btnFilter.setForeground(Color.WHITE);
        btnReset = new JButton("Reset");
        panelFilter.add(btnFilter);
        panelFilter.add(btnReset);

        String[] colsPesanan = {"ID Pesanan", "Waktu Transaksi", "Total Tagihan",
                                "Status", "Kurir", "Voucher", "Jml Item", "Metode"};
        mdlPesanan = new DefaultTableModel(colsPesanan, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPesanan = new JTable(mdlPesanan);
        tblPesanan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        String[] colsDetail = {"Nama Produk", "Merek", "Warna", "Ukuran", "Tipe",
                               "Jumlah", "Harga/pcs", "Subtotal"};
        mdlDetail = new DefaultTableModel(colsDetail, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDetail = new JTable(mdlDetail);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(tblPesanan), new JScrollPane(tblDetail));
        splitPane.setDividerLocation(200);
        splitPane.setBorder(BorderFactory.createTitledBorder(
            "Daftar Pesanan (atas) | Detail Item (bawah - klik baris pesanan)"));

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(panelFilter,  BorderLayout.NORTH);
        centerPanel.add(splitPane,    BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadPesanan());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);

        btnFilter.addActionListener(e -> loadPesanan());

        btnReset.addActionListener(e -> {
            cbTahun.setSelectedIndex(0);
            cbBulan.setSelectedIndex(0);
            cbStatus.setSelectedIndex(0);
            loadPesanan();
        });

        tblPesanan.getSelectionModel().addListSelectionListener(e -> {
            int row = tblPesanan.getSelectedRow();
            if (row >= 0) {
                int idPesanan = Integer.parseInt(tblPesanan.getValueAt(row, 0).toString());
                loadDetail(idPesanan);
            }
        });

        loadPesanan();
    }

    private void loadPesanan() {
        mdlPesanan.setRowCount(0);
        mdlDetail.setRowCount(0);

        Integer filterTahun = null;
        Integer filterBulan = null;
        String  filterStatus = cbStatus.getSelectedItem().toString();

        String tahunStr = cbTahun.getSelectedItem().toString();
        if (!tahunStr.equals("Semua")) filterTahun = Integer.parseInt(tahunStr);

        String bulanStr = cbBulan.getSelectedItem().toString();
        if (!bulanStr.equals("Semua")) {
            filterBulan = Integer.parseInt(bulanStr.split("-")[0]);
        }

        List<Object[]> list = frontendDAO.getRiwayatPesanan(
            SessionManager.getIdPelanggan(), filterTahun, filterBulan, filterStatus);

        for (Object[] row : list) {
            mdlPesanan.addRow(new Object[]{
                row[0], row[1],
                String.format("Rp %,.0f", (double) row[2]),
                row[3], row[4], row[5], row[6], row[7]
            });
        }

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Tidak ada pesanan yang sesuai dengan filter.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadDetail(int idPesanan) {
        mdlDetail.setRowCount(0);
        List<Object[]> list = frontendDAO.getDetailPesananLengkap(idPesanan);
        for (Object[] row : list) {
            mdlDetail.addRow(new Object[]{
                row[0], row[1], row[2], row[3], row[4], row[5],
                String.format("Rp %,.0f", (double) row[6]),
                String.format("Rp %,.0f", (double) row[7])
            });
        }
    }
}
