package frontend;

import config.SessionManager;
import dao.OrderDAO;
import dao.SaldoDAO;
import dao.TransactionDAO;
import model.DetailPesanan;
import model.Pesanan;
import model.Voucher;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CheckoutPanel - Panel checkout pesanan yang HANYA memakai Saldo Zalora.
 *
 * Perubahan dari versi lama:
 * - Combo box metode pembayaran DIHAPUS dari checkout pesanan
 * - Metode pembayaran ditetapkan secara otomatis ke "Saldo Zalora"
 * - Saldo pelanggan ditampilkan dan dibandingkan dengan total tagihan
 * - Status pesanan otomatis "Diproses" (bukan "Menunggu Pembayaran")
 * - Pengurangan saldo dilakukan di TransactionDAO dalam satu transaction SQL,
 *   BUKAN di panel ini
 *
 * Metode pembayaran lama (Bank, E-Wallet, dll) sudah DIPINDAH ke TopUpSaldoPanel
 * dan hanya digunakan untuk top up saldo, bukan untuk checkout pesanan.
 */
public class CheckoutPanel extends JPanel {
    private OrderDAO orderDAO;
    private TransactionDAO txDAO;
    private SaldoDAO saldoDAO;
    private CustomerDashboardFrame parentFrame;
    private List<Object[]> keranjangItems;

    // Komponen form
    private JComboBox<String> cbAlamat, cbKurir;
    private JTextField tfVoucher;
    private JButton btnCekVoucher, btnBayar;

    // Label ringkasan pembayaran
    private JLabel lblSubtotal, lblOngkir, lblDiskon, lblTotal;
    private JLabel lblSaldoSekarang, lblSisaSaldo, lblWarningPembayaran;

    // Data dari database
    private List<Object[]> listKurir  = new ArrayList<>();
    private List<Object[]> listAlamat = new ArrayList<>();

    // State checkout
    private double subtotal  = 0;
    private double diskon    = 0;
    private Voucher voucherAktif = null;
    private int idMetodeSaldoZalora = -1;

    private static final double ONGKIR_BASE = 15000;

    public CheckoutPanel(CustomerDashboardFrame frame) {
        this.parentFrame = frame;
        this.orderDAO    = new OrderDAO();
        this.txDAO       = new TransactionDAO();
        this.saldoDAO    = new SaldoDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Proses Checkout & Pembayaran");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);
    }

    /**
     * Dipanggil dari CartPanel saat pelanggan klik "Lanjut ke Checkout".
     * Membangun ulang seluruh UI checkout sesuai isi keranjang.
     */
    public void loadCheckout(List<Object[]> items) {
        this.keranjangItems = items;
        removeAll();

        JLabel lblTitle = new JLabel("Proses Checkout & Pembayaran");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        // Hitung subtotal dari isi keranjang
        subtotal = 0;
        for (Object[] item : items) subtotal += (double) item[6];

        // Ambil data dari database
        listKurir   = orderDAO.getAllKurirForCombo();
        listAlamat  = orderDAO.getAlamatForCombo(SessionManager.getIdPelanggan());
        idMetodeSaldoZalora = orderDAO.getIdMetodeSaldoZalora(); // Ambil ID metode Saldo Zalora

        double saldoPelanggan = saldoDAO.getSaldoPelanggan(SessionManager.getIdPelanggan());
        double totalAwal      = subtotal + ONGKIR_BASE;

        // ===== FORM: Alamat & Kurir =====
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Informasi Pengiriman"));

        formPanel.add(new JLabel("Alamat Pengiriman:"));
        cbAlamat = new JComboBox<>();
        if (listAlamat.isEmpty()) {
            cbAlamat.addItem("-- Belum ada alamat, tambah di profil --");
        } else {
            for (Object[] a : listAlamat) cbAlamat.addItem(a[1].toString());
        }
        formPanel.add(cbAlamat);

        formPanel.add(new JLabel("Kurir & Layanan:"));
        cbKurir = new JComboBox<>();
        for (Object[] k : listKurir) cbKurir.addItem(k[1].toString());
        formPanel.add(cbKurir);

        // Metode pembayaran sudah ditetapkan, tampilkan sebagai label
        formPanel.add(new JLabel("Metode Pembayaran:"));
        JLabel lblMetode = new JLabel("💳 Saldo Zalora");
        lblMetode.setFont(new Font("Arial", Font.BOLD, 13));
        lblMetode.setForeground(new Color(40, 167, 69));
        formPanel.add(lblMetode);

        formPanel.add(new JLabel("Kode Voucher (opsional):"));
        JPanel voucherPanel = new JPanel(new BorderLayout(5, 0));
        tfVoucher      = new JTextField();
        btnCekVoucher  = new JButton("Cek");
        voucherPanel.add(tfVoucher,     BorderLayout.CENTER);
        voucherPanel.add(btnCekVoucher, BorderLayout.EAST);
        formPanel.add(voucherPanel);

        // ===== RINGKASAN PEMBAYARAN =====
        JPanel ringkasanPanel = new JPanel(new GridLayout(7, 2, 8, 6));
        ringkasanPanel.setBorder(BorderFactory.createTitledBorder("Ringkasan Pembayaran & Saldo"));

        ringkasanPanel.add(new JLabel("Subtotal Produk:"));
        lblSubtotal = new JLabel(formatRupiah(subtotal));
        ringkasanPanel.add(lblSubtotal);

        ringkasanPanel.add(new JLabel("Biaya Pengiriman:"));
        lblOngkir = new JLabel(formatRupiah(ONGKIR_BASE));
        ringkasanPanel.add(lblOngkir);

        ringkasanPanel.add(new JLabel("Diskon Voucher:"));
        lblDiskon = new JLabel("Rp 0");
        lblDiskon.setForeground(Color.RED);
        ringkasanPanel.add(lblDiskon);

        ringkasanPanel.add(new JLabel("TOTAL TAGIHAN:"));
        lblTotal = new JLabel(formatRupiah(totalAwal));
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotal.setForeground(new Color(40, 167, 69));
        ringkasanPanel.add(lblTotal);

        // Batas pemisah saldo
        JSeparator sep = new JSeparator();
        ringkasanPanel.add(sep);
        ringkasanPanel.add(new JSeparator());

        ringkasanPanel.add(new JLabel("Saldo Zalora Anda:"));
        lblSaldoSekarang = new JLabel(saldoPelanggan >= 0 ? formatRupiah(saldoPelanggan) : "Gagal memuat");
        lblSaldoSekarang.setFont(new Font("Arial", Font.BOLD, 13));
        lblSaldoSekarang.setForeground(new Color(0, 102, 204));
        ringkasanPanel.add(lblSaldoSekarang);

        ringkasanPanel.add(new JLabel("Sisa Saldo Setelah Checkout:"));
        double sisaSaldo = saldoPelanggan - totalAwal;
        lblSisaSaldo = new JLabel(formatRupiah(sisaSaldo));
        lblSisaSaldo.setFont(new Font("Arial", Font.BOLD, 13));
        lblSisaSaldo.setForeground(sisaSaldo >= 0 ? new Color(40, 167, 69) : Color.RED);
        ringkasanPanel.add(lblSisaSaldo);

        // Warning jika saldo tidak cukup
        lblWarningPembayaran = new JLabel("");
        if (saldoPelanggan < totalAwal) {
            lblWarningPembayaran.setText("⚠ Saldo tidak cukup! Silakan top up terlebih dahulu.");
            lblWarningPembayaran.setForeground(Color.RED);
            lblWarningPembayaran.setFont(new Font("Arial", Font.BOLD, 12));
        }

        // ===== TOMBOL BAYAR =====
        btnBayar = new JButton("BAYAR DENGAN SALDO ZALORA");
        btnBayar.setBackground(new Color(40, 167, 69));
        btnBayar.setForeground(Color.WHITE);
        btnBayar.setFont(new Font("Arial", Font.BOLD, 14));
        btnBayar.setPreferredSize(new Dimension(280, 45));

        JButton btnKembali = new JButton("< Kembali ke Keranjang");
        btnKembali.addActionListener(e -> parentFrame.bukaKeranjang());

        // ===== LAYOUT UTAMA =====
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(formPanel,      BorderLayout.NORTH);
        centerPanel.add(ringkasanPanel, BorderLayout.CENTER);
        centerPanel.add(lblWarningPembayaran, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.add(btnKembali);
        bottomPanel.add(btnBayar);
        add(bottomPanel, BorderLayout.SOUTH);

        // ===== LISTENER CEK VOUCHER =====
        btnCekVoucher.addActionListener(e -> {
            String kode = tfVoucher.getText().trim();
            if (kode.isEmpty()) {
                voucherAktif = null; diskon = 0;
                hitungUlangTotal(saldoPelanggan);
                return;
            }
            Voucher v = txDAO.getVoucherByKode(kode);
            if (v == null) {
                JOptionPane.showMessageDialog(this, "Kode voucher tidak ditemukan!", "Info", JOptionPane.INFORMATION_MESSAGE);
                voucherAktif = null; diskon = 0;
            } else if (v.getKuotaPemakaian() <= 0) {
                JOptionPane.showMessageDialog(this, "Kuota voucher sudah habis!", "Info", JOptionPane.INFORMATION_MESSAGE);
                voucherAktif = null; diskon = 0;
            } else if (subtotal < v.getMinimumBelanja()) {
                JOptionPane.showMessageDialog(this,
                    String.format("Minimum belanja untuk voucher ini: %s", formatRupiah(v.getMinimumBelanja())),
                    "Info", JOptionPane.INFORMATION_MESSAGE);
                voucherAktif = null; diskon = 0;
            } else {
                voucherAktif = v;
                if (v.getTipeDiskon().equalsIgnoreCase("PERSENTASE")) {
                    diskon = subtotal * (v.getNilaiDiskon() / 100.0);
                } else {
                    diskon = v.getNilaiDiskon();
                }
                diskon = Math.min(diskon, subtotal);
                JOptionPane.showMessageDialog(this,
                    "Voucher valid! Diskon: " + formatRupiah(diskon));
            }
            hitungUlangTotal(saldoPelanggan);
        });

        // ===== LISTENER TOMBOL BAYAR =====
        btnBayar.addActionListener(e -> prosesCheckout());

        revalidate();
        repaint();
    }

    /**
     * Menghitung ulang total tagihan setelah voucher diubah,
     * dan memperbarui tampilan sisa saldo secara real-time.
     */
    private void hitungUlangTotal(double saldoPelanggan) {
        double total    = subtotal + ONGKIR_BASE - diskon;
        double sisaSaldo = saldoPelanggan - total;

        lblDiskon.setText(formatRupiah(diskon));
        lblTotal.setText(formatRupiah(total));
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));

        lblSisaSaldo.setText(formatRupiah(sisaSaldo));
        lblSisaSaldo.setForeground(sisaSaldo >= 0 ? new Color(40, 167, 69) : Color.RED);

        if (saldoPelanggan < total) {
            lblWarningPembayaran.setText("⚠ Saldo tidak cukup! Silakan top up terlebih dahulu.");
            lblWarningPembayaran.setForeground(Color.RED);
        } else {
            lblWarningPembayaran.setText("✓ Saldo mencukupi. Siap checkout!");
            lblWarningPembayaran.setForeground(new Color(40, 167, 69));
        }
    }

    /**
     * Memproses checkout pesanan menggunakan Saldo Zalora.
     *
     * Alur:
     * 1. Validasi alamat, kurir, dan ID metode Saldo Zalora
     * 2. Cek saldo awal (validasi awal di GUI, validasi final di DAO)
     * 3. Buat object Pesanan dengan status "Diproses"
     * 4. Panggil insertTransaksiLengkapDenganSaldoZalora() dari TransactionDAO
     *    - DAO yang mengurusi transaction: saldo, pesanan, stok, voucher, riwayat
     * 5. Jika berhasil: kosongkan keranjang, pindah ke riwayat
     * 6. Jika gagal: tampilkan pesan error, keranjang tetap ada
     */
    private void prosesCheckout() {
        // ---- VALIDASI ALAMAT ----
        if (listAlamat.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Tambahkan alamat pengiriman terlebih dahulu!\n"
                + "Hubungi admin untuk menambah alamat.",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ---- VALIDASI KURIR ----
        if (listKurir.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Data kurir tidak tersedia! Hubungi admin.",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ---- VALIDASI ID METODE SALDO ZALORA ----
        if (idMetodeSaldoZalora == -1) {
            JOptionPane.showMessageDialog(this,
                "Konfigurasi metode 'Saldo Zalora' tidak ditemukan di database!\n"
                + "Pastikan script SQL_TAMBAHAN_JALANKAN_DULU.sql sudah dijalankan.",
                "Error Konfigurasi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ---- AMBIL PILIHAN ----
        int idxKurir  = cbKurir.getSelectedIndex();
        int idxAlamat = cbAlamat.getSelectedIndex();

        int    idKurir     = (int)    listKurir.get(idxKurir)[0];
        String labelAlamat = (String) listAlamat.get(idxAlamat)[0];
        double total       = subtotal + ONGKIR_BASE - diskon;

        // ---- VALIDASI SALDO (awal di GUI, final di DAO) ----
        double saldoSaatIni = saldoDAO.getSaldoPelanggan(SessionManager.getIdPelanggan());
        if (saldoSaatIni < total) {
            JOptionPane.showMessageDialog(this,
                String.format("Saldo Zalora tidak cukup!\n\n"
                            + "Saldo Anda   : %s\n"
                            + "Total Tagihan: %s\n"
                            + "Kekurangan   : %s\n\n"
                            + "Silakan top up saldo terlebih dahulu.",
                    formatRupiah(saldoSaatIni),
                    formatRupiah(total),
                    formatRupiah(total - saldoSaatIni)),
                "Saldo Tidak Cukup", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ---- BUAT OBJECT PESANAN ----
        Pesanan pesanan = new Pesanan();
        pesanan.setIdPesanan(orderDAO.getNextIdPesanan());
        pesanan.setTotalHargaBarang(subtotal);
        pesanan.setBiayaPengiriman(ONGKIR_BASE);
        pesanan.setTotalTagihan(total);
        pesanan.setStatusPesanan("Diproses"); // Langsung Diproses karena saldo langsung terpotong
        pesanan.setLabelAlamat(labelAlamat);
        pesanan.setIdPelanggan(SessionManager.getIdPelanggan());
        pesanan.setIdMetode(idMetodeSaldoZalora); // ID metode Saldo Zalora dari DB, bukan dari combo
        pesanan.setIdKurir(idKurir);
        pesanan.setKodeVoucher(voucherAktif != null ? voucherAktif.getKodeVoucher() : null);

        // ---- BUAT LIST DETAIL PESANAN ----
        List<DetailPesanan> listDetail = new ArrayList<>();
        for (Object[] item : keranjangItems) {
            DetailPesanan dp = new DetailPesanan();
            dp.setIdPesanan(pesanan.getIdPesanan());
            dp.setIdVarian((int)    item[0]);
            dp.setJumlahBeli((int)  item[5]);
            dp.setHargaSaatBeli((double) item[4]);
            dp.setSubtotal((double) item[6]);
            listDetail.add(dp);
        }

        // ---- KONFIRMASI CHECKOUT ----
        int konfirm = JOptionPane.showConfirmDialog(this,
            String.format("Konfirmasi Pembayaran dengan Saldo Zalora:\n\n"
                        + "Total Tagihan  : %s\n"
                        + "Saldo Sekarang : %s\n"
                        + "Sisa Saldo     : %s\n\n"
                        + "Lanjutkan checkout?",
                formatRupiah(total),
                formatRupiah(saldoSaatIni),
                formatRupiah(saldoSaatIni - total)),
            "Konfirmasi Checkout", JOptionPane.YES_NO_OPTION);

        if (konfirm != JOptionPane.YES_OPTION) return;

        // ---- PROSES CHECKOUT DI DAO (satu transaction SQL) ----
        // TransactionDAO.insertTransaksiLengkapDenganSaldoZalora() bertanggung jawab atas:
        // 1. Validasi saldo final (dengan UPDLOCK agar aman)
        // 2. INSERT PESANAN
        // 3. INSERT DETAIL_PESANAN
        // 4. UPDATE stok varian
        // 5. UPDATE kuota voucher (jika ada)
        // 6. UPDATE saldo pelanggan (berkurang)
        // 7. INSERT riwayat saldo (PEMBAYARAN)
        // 8. COMMIT atau ROLLBACK
        boolean berhasil = txDAO.insertTransaksiLengkapDenganSaldoZalora(pesanan, listDetail);

        if (berhasil) {
            JOptionPane.showMessageDialog(this,
                String.format("Pembayaran berhasil! 🎉\n\n"
                            + "Pesanan #%d sedang Diproses.\n"
                            + "Saldo Zalora berkurang %s.\n\n"
                            + "Terima kasih telah berbelanja di Zalora!",
                    pesanan.getIdPesanan(),
                    formatRupiah(total)),
                "Transaksi Berhasil", JOptionPane.INFORMATION_MESSAGE);
            parentFrame.setelahCheckoutBerhasil();
        } else {
            JOptionPane.showMessageDialog(this,
                "Transaksi gagal!\n\n"
                + "Kemungkinan penyebab:\n"
                + "• Saldo tidak mencukupi\n"
                + "• Stok produk habis\n"
                + "• Koneksi database bermasalah\n\n"
                + "Silakan coba lagi atau hubungi admin.",
                "Transaksi Gagal", JOptionPane.ERROR_MESSAGE);
            // Keranjang TIDAK dikosongkan jika transaksi gagal
        }
    }

    /** Format angka sebagai rupiah */
    private String formatRupiah(double nilai) {
        return String.format("Rp %,.0f", nilai);
    }
}
