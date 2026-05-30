package backend;

import dao.OrderDAO;
import dao.SaldoDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * OrderManagementPanel - Panel admin untuk memantau dan mengelola pesanan.
 *
 * Pembaruan dari versi lama:
 * - Ketika admin mengubah status pesanan menjadi "Dibatalkan",
 *   sistem otomatis menawarkan refund saldo ke pelanggan.
 * - Refund dilakukan melalui SaldoDAO.refundSaldo() dalam transaction SQL.
 * - Refund tidak bisa dilakukan dua kali pada pesanan yang sama (cek duplikat).
 */
public class OrderManagementPanel extends JPanel {
    private OrderDAO  orderDAO;
    private SaldoDAO  saldoDAO;
    private JTable    tblPesanan, tblDetail;
    private DefaultTableModel mdlPesanan, mdlDetail;
    private JComboBox<String> cbStatusFilter;
    private JTextField tfSearchPelanggan;

    public OrderManagementPanel() {
        this.orderDAO = new OrderDAO();
        this.saldoDAO = new SaldoDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Pantau & Kelola Pesanan Pelanggan");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        // ===== PANEL FILTER & SEARCH =====
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Pencarian"));
        filterPanel.add(new JLabel("Nama Pelanggan:"));
        tfSearchPelanggan = new JTextField(15);
        filterPanel.add(tfSearchPelanggan);
        filterPanel.add(new JLabel("Status:"));
        cbStatusFilter = new JComboBox<>(new String[]{
            "Semua", "Menunggu Pembayaran", "Diproses", "Dikirim", "Selesai", "Dibatalkan"
        });
        filterPanel.add(cbStatusFilter);
        JButton btnCari = new JButton("Cari / Refresh");
        filterPanel.add(btnCari);

        // ===== TABEL PESANAN =====
        String[] colsPesanan = {
            "ID Pesanan", "Pelanggan", "Waktu Transaksi", "Total Tagihan", "Status", "Voucher", "Kurir"
        };
        mdlPesanan = new DefaultTableModel(colsPesanan, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPesanan = new JTable(mdlPesanan);
        tblPesanan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Lebar kolom
        tblPesanan.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblPesanan.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblPesanan.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblPesanan.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblPesanan.getColumnModel().getColumn(4).setPreferredWidth(110);

        // ===== TABEL DETAIL ITEM =====
        String[] colsDetail = {
            "Nama Produk", "Warna", "Ukuran", "Jumlah", "Harga/pcs", "Subtotal"
        };
        mdlDetail = new DefaultTableModel(colsDetail, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDetail = new JTable(mdlDetail);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(tblPesanan), new JScrollPane(tblDetail));
        splitPane.setDividerLocation(280);
        splitPane.setBorder(BorderFactory.createTitledBorder(
            "Daftar Pesanan (atas) | Detail Item (bawah — klik baris pesanan)"));

        // ===== PANEL AKSI UPDATE STATUS =====
        JPanel aksiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        aksiPanel.setBorder(BorderFactory.createTitledBorder("Update Status Pesanan Terpilih"));

        JComboBox<String> cbStatusBaru = new JComboBox<>(new String[]{
            "Menunggu Pembayaran", "Diproses", "Dikirim", "Selesai", "Dibatalkan"
        });
        JButton btnUpdateStatus = new JButton("Update Status");
        btnUpdateStatus.setBackground(new Color(0, 123, 255));
        btnUpdateStatus.setForeground(Color.WHITE);

        aksiPanel.add(new JLabel("Ubah Status ke:"));
        aksiPanel.add(cbStatusBaru);
        aksiPanel.add(btnUpdateStatus);

        // Info refund
        JLabel lblInfoRefund = new JLabel(
            "  ℹ Jika status diubah ke \"Dibatalkan\", sistem akan menawarkan refund saldo ke pelanggan."
        );
        lblInfoRefund.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfoRefund.setForeground(new Color(100, 100, 100));
        aksiPanel.add(lblInfoRefund);

        // ===== GABUNGKAN =====
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(splitPane,  BorderLayout.CENTER);
        centerPanel.add(aksiPanel,  BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // ===== EVENT LISTENERS =====

        // Klik baris pesanan → tampilkan detail item
        tblPesanan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblPesanan.getSelectedRow();
                if (row >= 0) {
                    int idPesanan = Integer.parseInt(tblPesanan.getValueAt(row, 0).toString());
                    loadDetailPesanan(idPesanan);
                }
            }
        });

        btnCari.addActionListener(e -> loadSemuaPesanan());

        btnUpdateStatus.addActionListener(e -> {
            int row = tblPesanan.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this,
                    "Pilih baris pesanan terlebih dahulu!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int    idPesanan   = Integer.parseInt(tblPesanan.getValueAt(row, 0).toString());
            String statusLama  = tblPesanan.getValueAt(row, 4).toString();
            String statusBaru  = cbStatusBaru.getSelectedItem().toString();

            // Jangan update kalau status sama
            if (statusLama.equals(statusBaru)) {
                JOptionPane.showMessageDialog(this,
                    "Status pesanan sudah \"" + statusBaru + "\". Tidak ada perubahan.");
                return;
            }

            // ---- KASUS KHUSUS: DIBATALKAN → TAWARKAN REFUND SALDO ----
            if (statusBaru.equals("Dibatalkan")) {
                prosesUbahStatusKeDibatalkan(idPesanan, statusLama);
            } else {
                // Update status biasa tanpa refund
                prosesUpdateStatusBiasa(idPesanan, statusBaru);
            }
        });

        // Load data saat panel pertama kali dibuka
        loadSemuaPesanan();
    }

    /**
     * Memproses perubahan status pesanan ke "Dibatalkan".
     *
     * Logika:
     * 1. Cek apakah pesanan pernah direfund sebelumnya
     * 2. Jika belum pernah dan statusnya menunjukkan sudah dibayar, tawarkan refund
     * 3. Jika admin setuju refund → SaldoDAO.refundSaldo() (transaction SQL)
     * 4. Jika admin tidak mau refund → hanya update status
     * 5. Commit semua perubahan
     *
     * @param idPesanan  ID pesanan yang akan dibatalkan
     * @param statusLama Status pesanan sebelum dibatalkan
     */
    private void prosesUbahStatusKeDibatalkan(int idPesanan, String statusLama) {
        // Status yang menandakan pesanan sudah dibayar dengan Saldo Zalora
        boolean sudahDibayar = statusLama.equals("Diproses")
                            || statusLama.equals("Dikirim")
                            || statusLama.equals("Selesai");

        boolean sudahPernahRefund = saldoDAO.isSudahPernahRefund(idPesanan);

        if (sudahDibayar && !sudahPernahRefund) {
            // Pesanan sudah dibayar tapi belum pernah direfund → tawarkan refund
            int pilihanRefund = JOptionPane.showConfirmDialog(this,
                "Pesanan #" + idPesanan + " akan dibatalkan.\n\n"
                + "Status saat ini: " + statusLama + "\n"
                + "Pesanan ini sepertinya sudah dibayar menggunakan Saldo Zalora.\n\n"
                + "Apakah saldo pelanggan perlu dikembalikan (refund)?",
                "Konfirmasi Pembatalan + Refund",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (pilihanRefund == JOptionPane.CANCEL_OPTION) return;

            if (pilihanRefund == JOptionPane.YES_OPTION) {
                // Refund saldo + update status dalam satu transaction
                boolean refundOk = saldoDAO.refundSaldo(idPesanan);
                if (refundOk) {
                    // Setelah refund berhasil, update status ke Dibatalkan
                    boolean updateOk = orderDAO.updateStatusPesanan(idPesanan, "Dibatalkan");
                    if (updateOk) {
                        JOptionPane.showMessageDialog(this,
                            "Pesanan #" + idPesanan + " berhasil dibatalkan.\n"
                            + "Saldo pelanggan sudah dikembalikan (refund).",
                            "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Refund berhasil, tetapi gagal update status pesanan!\n"
                            + "Periksa database secara manual.",
                            "Peringatan", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Gagal melakukan refund!\n"
                        + "Pesanan tidak dibatalkan.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // Admin memilih TIDAK refund → hanya update status
                prosesUpdateStatusBiasa(idPesanan, "Dibatalkan");
            }
        } else if (sudahPernahRefund) {
            // Sudah pernah direfund sebelumnya, hanya update status
            int konfirm = JOptionPane.showConfirmDialog(this,
                "Pesanan #" + idPesanan + " akan dibatalkan.\n"
                + "Catatan: Pesanan ini sudah pernah direfund sebelumnya.\n\n"
                + "Lanjutkan update status ke Dibatalkan?",
                "Konfirmasi Pembatalan", JOptionPane.YES_NO_OPTION);
            if (konfirm == JOptionPane.YES_OPTION) {
                prosesUpdateStatusBiasa(idPesanan, "Dibatalkan");
            }
        } else {
            // Status belum dibayar (misal: Menunggu Pembayaran) → tidak perlu refund
            int konfirm = JOptionPane.showConfirmDialog(this,
                "Update status pesanan #" + idPesanan + " menjadi \"Dibatalkan\"?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (konfirm == JOptionPane.YES_OPTION) {
                prosesUpdateStatusBiasa(idPesanan, "Dibatalkan");
            }
        }

        loadSemuaPesanan();
    }

    /** Update status pesanan biasa tanpa logika refund */
    private void prosesUpdateStatusBiasa(int idPesanan, String statusBaru) {
        int konfirm = JOptionPane.showConfirmDialog(this,
            "Update status pesanan #" + idPesanan + " menjadi \"" + statusBaru + "\"?",
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirm != JOptionPane.YES_OPTION) return;

        if (orderDAO.updateStatusPesanan(idPesanan, statusBaru)) {
            JOptionPane.showMessageDialog(this,
                "Status pesanan #" + idPesanan + " berhasil diperbarui menjadi \"" + statusBaru + "\"!",
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Gagal update status pesanan!",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        loadSemuaPesanan();
    }

    /** Memuat semua pesanan dari database, dengan filter aktif */
    private void loadSemuaPesanan() {
        mdlPesanan.setRowCount(0);
        mdlDetail.setRowCount(0);

        String filterNama   = tfSearchPelanggan.getText().trim().toLowerCase();
        String filterStatus = cbStatusFilter.getSelectedItem().toString();

        List<Object[]> list = orderDAO.getAllPesananAdmin();
        for (Object[] row : list) {
            String namaPelanggan = row[1].toString().toLowerCase();
            String status        = row[4].toString();
            boolean namaMatch   = filterNama.isEmpty() || namaPelanggan.contains(filterNama);
            boolean statusMatch = filterStatus.equals("Semua") || status.equals(filterStatus);
            if (namaMatch && statusMatch) {
                // Format total tagihan sebagai rupiah di tabel
                Object[] rowDisplay = row.clone();
                if (rowDisplay[3] instanceof Number) {
                    rowDisplay[3] = String.format("Rp %,.0f", ((Number) rowDisplay[3]).doubleValue());
                }
                mdlPesanan.addRow(rowDisplay);
            }
        }
    }

    /** Memuat detail item dari pesanan yang diklik */
    private void loadDetailPesanan(int idPesanan) {
        mdlDetail.setRowCount(0);
        List<Object[]> list = orderDAO.getDetailPesanan(idPesanan);
        for (Object[] row : list) {
            Object[] rowDisplay = row.clone();
            // Format harga dan subtotal sebagai rupiah
            if (rowDisplay[4] instanceof Number)
                rowDisplay[4] = String.format("Rp %,.0f", ((Number) rowDisplay[4]).doubleValue());
            if (rowDisplay[5] instanceof Number)
                rowDisplay[5] = String.format("Rp %,.0f", ((Number) rowDisplay[5]).doubleValue());
            mdlDetail.addRow(rowDisplay);
        }
    }
}
