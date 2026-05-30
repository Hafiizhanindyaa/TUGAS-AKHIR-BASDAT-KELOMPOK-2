package backend;

import dao.AnalisisDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * TopPelangganPanel.java
 * Panel admin untuk menampilkan TOP 5 Pelanggan dengan Belanja Terbanyak.
 *
 * Soal (b): Apa 5 pembeli yang memiliki pembelian tertinggi
 *           dalam kurun waktu tertentu?
 *
 * Teknik SQL yang digunakan:
 *   - STORED PROCEDURE (SP_TOP5_PELANGGAN_TERBANYAK_BELANJA)
 *   - Di dalam SP: JOIN + GROUP BY + SUM + ORDER BY + TOP 5 + Subquery (pesanan terakhir)
 *   - Java memanggil via CallableStatement dengan parameter bulan/tahun opsional
 *   - Filter bulan & tahun bisa dikosongkan (NULL) → tampilkan semua waktu
 */
public class TopPelangganPanel extends JPanel {

    private AnalisisDAO analisisDAO;
    private DefaultTableModel tableModel;
    private JTable tabelPelanggan;

    // Komponen filter
    private JComboBox<String> cbBulan;
    private JComboBox<String> cbTahun;
    private JLabel lblStatusFilter;

    private static final NumberFormat fmt = NumberFormat.getInstance(new Locale("id", "ID"));
    private static final String[] NAMA_BULAN = {
        "Semua Bulan", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    public TopPelangganPanel() {
        analisisDAO = new AnalisisDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ---- HEADER ----
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(40, 167, 69));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lblJudul = new JLabel("TOP 5 Pelanggan dengan Belanja Terbanyak");
        lblJudul.setFont(new Font("Arial", Font.BOLD, 17));
        lblJudul.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(
            "Data diambil via STORED PROCEDURE  |  Teknik: SP + Subquery + GROUP BY + SUM + Parameter Filter");
        lblSub.setFont(new Font("Arial", Font.ITALIC, 11));
        lblSub.setForeground(new Color(200, 255, 210));

        panelHeader.add(lblJudul, BorderLayout.NORTH);
        panelHeader.add(lblSub,   BorderLayout.SOUTH);
        add(panelHeader, BorderLayout.NORTH);

        // ---- PANEL FILTER ----
        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        panelFilter.setBorder(BorderFactory.createTitledBorder("Filter Periode Waktu (opsional)"));

        panelFilter.add(new JLabel("Bulan:"));
        cbBulan = new JComboBox<>(NAMA_BULAN);
        cbBulan.setPreferredSize(new Dimension(120, 28));
        panelFilter.add(cbBulan);

        panelFilter.add(Box.createHorizontalStrut(10));
        panelFilter.add(new JLabel("Tahun:"));

        // Isi tahun dari database + opsi "Semua Tahun"
        cbTahun = new JComboBox<>();
        cbTahun.addItem("Semua Tahun");
        List<Integer> tahunList = analisisDAO.getTahunTersedia();
        for (Integer t : tahunList) cbTahun.addItem(String.valueOf(t));
        cbTahun.setPreferredSize(new Dimension(110, 28));
        panelFilter.add(cbTahun);

        panelFilter.add(Box.createHorizontalStrut(10));
        JButton btnTampilkan = new JButton("Tampilkan");
        btnTampilkan.setBackground(new Color(40, 167, 69));
        btnTampilkan.setForeground(Color.WHITE);
        btnTampilkan.setFocusPainted(false);
        panelFilter.add(btnTampilkan);

        JButton btnReset = new JButton("Reset Filter");
        btnReset.setFocusPainted(false);
        panelFilter.add(btnReset);

        lblStatusFilter = new JLabel("");
        lblStatusFilter.setFont(new Font("Arial", Font.ITALIC, 11));
        lblStatusFilter.setForeground(new Color(80, 80, 80));
        panelFilter.add(lblStatusFilter);

        add(panelFilter, BorderLayout.NORTH);

        // ---- TABEL ----
        String[] kolom = {
            "Rank", "ID", "Nama Akun", "Surel",
            "Jml Pesanan", "Total Belanja (Rp)", "Rata-rata Belanja (Rp)", "Pesanan Terakhir"
        };
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelPelanggan = new JTable(tableModel);
        tabelPelanggan.setRowHeight(28);
        tabelPelanggan.setFont(new Font("Arial", Font.PLAIN, 13));
        tabelPelanggan.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tabelPelanggan.getTableHeader().setBackground(new Color(40, 167, 69));
        tabelPelanggan.getTableHeader().setForeground(Color.WHITE);
        tabelPelanggan.setSelectionBackground(new Color(144, 238, 144));

        int[] lebar = {40, 40, 150, 180, 90, 160, 170, 140};
        for (int i = 0; i < lebar.length; i++) {
            tabelPelanggan.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }

        // Highlight ranking 1-3
        tabelPelanggan.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if      (row == 0) c.setBackground(new Color(255, 215, 0, 80));
                    else if (row == 1) c.setBackground(new Color(192, 192, 192, 60));
                    else if (row == 2) c.setBackground(new Color(205, 127, 50, 60));
                    else               c.setBackground(Color.WHITE);
                }
                if (column == 0 || column == 1 || column == 4) {
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                return c;
            }
        });

        // Bungkus header dan tabel dalam satu panel agar bisa ditaruh di CENTER
        JPanel panelTengah = new JPanel(new BorderLayout());
        panelTengah.add(panelFilter, BorderLayout.NORTH);
        panelTengah.add(new JScrollPane(tabelPelanggan), BorderLayout.CENTER);
        add(panelTengah, BorderLayout.CENTER);

        // ---- PANEL BAWAH ----
        JPanel panelBawah = new JPanel(new BorderLayout());
        panelBawah.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JLabel lblKet = new JLabel(
            "<html><i>* Pesanan 'Dibatalkan' tidak dihitung.  " +
            "Stored Procedure menerima parameter @BulanFilter dan @TahunFilter (NULL = semua waktu).</i></html>");
        lblKet.setFont(new Font("Arial", Font.PLAIN, 11));
        lblKet.setForeground(Color.GRAY);

        JButton btnRefresh = new JButton("↺  Refresh");
        btnRefresh.setFocusPainted(false);
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBtn.add(btnRefresh);

        panelBawah.add(lblKet,   BorderLayout.CENTER);
        panelBawah.add(panelBtn, BorderLayout.EAST);
        add(panelBawah, BorderLayout.SOUTH);

        // ---- LOAD DATA AWAL (semua waktu) ----
        loadData(null, null);

        // ---- AKSI TOMBOL ----
        btnTampilkan.addActionListener(e -> {
            Integer bulan = cbBulan.getSelectedIndex() == 0 ? null : cbBulan.getSelectedIndex(); // index 0=semua, 1=Jan, dst
            Integer tahun = null;
            String tahunStr = (String) cbTahun.getSelectedItem();
            if (tahunStr != null && !tahunStr.equals("Semua Tahun")) {
                try { tahun = Integer.parseInt(tahunStr); } catch (NumberFormatException ignored) {}
            }
            loadData(bulan, tahun);
        });

        btnReset.addActionListener(e -> {
            cbBulan.setSelectedIndex(0);
            cbTahun.setSelectedIndex(0);
            loadData(null, null);
        });

        btnRefresh.addActionListener(e -> {
            Integer bulan = cbBulan.getSelectedIndex() == 0 ? null : cbBulan.getSelectedIndex();
            Integer tahun = null;
            String tahunStr = (String) cbTahun.getSelectedItem();
            if (tahunStr != null && !tahunStr.equals("Semua Tahun")) {
                try { tahun = Integer.parseInt(tahunStr); } catch (NumberFormatException ignored) {}
            }
            loadData(bulan, tahun);
        });
    }

    private void loadData(Integer bulan, Integer tahun) {
        tableModel.setRowCount(0);

        // Update label status filter
        String statusFilter = "Menampilkan: ";
        statusFilter += (bulan == null) ? "Semua Bulan" : NAMA_BULAN[bulan];
        statusFilter += " / ";
        statusFilter += (tahun == null) ? "Semua Tahun" : tahun.toString();
        lblStatusFilter.setText("[ " + statusFilter + " ]");

        // Panggil Stored Procedure via DAO
        List<Object[]> list = analisisDAO.getTop5PelangganTerbelajaViaStoredProc(bulan, tahun);

        if (list.isEmpty()) {
            tableModel.addRow(new Object[]{
                "-", "-", "Belum ada data transaksi untuk periode ini",
                "-", 0, "Rp 0", "Rp 0", "-"
            });
            return;
        }

        for (Object[] row : list) {
            // row: [Rank, ID, NamaAkun, Surel, JmlPesanan, TotalBelanja, RataRata, PesananTerakhir]
            tableModel.addRow(new Object[]{
                "#" + row[0],
                row[1],
                row[2],
                row[3],
                row[4] + "x",
                "Rp " + fmt.format(row[5]),
                "Rp " + fmt.format(row[6]),
                row[7]
            });
        }
    }
} // AKHIR DARI FILE TopPelangganPanel.java
