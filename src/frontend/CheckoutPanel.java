package frontend;

import config.SessionManager;
import dao.FrontendDAO;
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

public class CheckoutPanel extends JPanel {
    private FrontendDAO frontendDAO;
    private OrderDAO orderDAO;
    private SaldoDAO saldoDAO;
    private TransactionDAO txDAO;
    private CustomerDashboardFrame parentFrame;
    private List<Object[]> keranjangItems;

    private JComboBox<String> cbAlamat, cbKurir;
    private JTextField tfVoucher;
    private JButton btnCekVoucher, btnBayar;
    private JLabel lblSubtotal, lblOngkir, lblDiskon, lblTotal;
    private JLabel lblSaldoSekarang, lblSisaSaldo, lblWarningPembayaran;

    private List<Object[]> listKurir  = new ArrayList<>();
    private List<Object[]> listAlamat = new ArrayList<>();

    private double subtotal = 0;
    private double diskon   = 0;
    private Voucher voucherAktif     = null;
    private int idMetodeSaldoZalora  = -1;

    private static final double ONGKIR_BASE = 15000;

    public CheckoutPanel(CustomerDashboardFrame frame) {
        this.parentFrame  = frame;
        this.frontendDAO  = new FrontendDAO();
        this.orderDAO     = new OrderDAO();
        this.saldoDAO     = new SaldoDAO();
        this.txDAO        = new TransactionDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Proses Checkout & Pembayaran");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);
    }

    public void loadCheckout(List<Object[]> items) {
        this.keranjangItems = items;
        removeAll();

        JLabel lblTitle = new JLabel("Proses Checkout & Pembayaran");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        subtotal = 0;
        for (Object[] item : items) subtotal += (double) item[6];

        listKurir              = orderDAO.getAllKurirForCombo();
        listAlamat             = orderDAO.getAlamatForCombo(SessionManager.getIdPelanggan());
        idMetodeSaldoZalora    = orderDAO.getIdMetodeSaldoZalora();
        double saldoPelanggan  = saldoDAO.getSaldoPelanggan(SessionManager.getIdPelanggan());
        double totalAwal       = subtotal + ONGKIR_BASE;

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Informasi Pengiriman"));

        formPanel.add(new JLabel("Alamat Pengiriman:"));
        cbAlamat = new JComboBox<>();
        if (listAlamat.isEmpty()) cbAlamat.addItem("-- Belum ada alamat, tambah di profil --");
        else for (Object[] a : listAlamat) cbAlamat.addItem(a[1].toString());
        formPanel.add(cbAlamat);

        formPanel.add(new JLabel("Kurir & Layanan:"));
        cbKurir = new JComboBox<>();
        for (Object[] k : listKurir) cbKurir.addItem(k[1].toString());
        formPanel.add(cbKurir);

        formPanel.add(new JLabel("Metode Pembayaran:"));
        JLabel lblMetode = new JLabel("Saldo Zalora");
        lblMetode.setFont(new Font("Arial", Font.BOLD, 13));
        lblMetode.setForeground(new Color(40, 167, 69));
        formPanel.add(lblMetode);

        formPanel.add(new JLabel("Kode Voucher (opsional):"));
        JPanel voucherPanel = new JPanel(new BorderLayout(5, 0));
        tfVoucher     = new JTextField();
        btnCekVoucher = new JButton("Cek");
        voucherPanel.add(tfVoucher, BorderLayout.CENTER);
        voucherPanel.add(btnCekVoucher, BorderLayout.EAST);
        formPanel.add(voucherPanel);

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

        ringkasanPanel.add(new JSeparator()); ringkasanPanel.add(new JSeparator());

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

        lblWarningPembayaran = new JLabel("");
        if (saldoPelanggan < totalAwal) {
            lblWarningPembayaran.setText("[!] Saldo tidak cukup! Silakan top up terlebih dahulu.");
            lblWarningPembayaran.setForeground(Color.RED);
            lblWarningPembayaran.setFont(new Font("Arial", Font.BOLD, 12));
        }

        btnBayar = new JButton("BAYAR DENGAN SALDO ZALORA  [via SP_SIMULASI_CHECKOUT]");
        btnBayar.setBackground(new Color(40, 167, 69));
        btnBayar.setForeground(Color.WHITE);
        btnBayar.setFont(new Font("Arial", Font.BOLD, 13));
        btnBayar.setPreferredSize(new Dimension(380, 45));

        JButton btnKembali = new JButton("< Kembali ke Keranjang");
        btnKembali.addActionListener(e -> parentFrame.bukaKeranjang());

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(formPanel,            BorderLayout.NORTH);
        centerPanel.add(ringkasanPanel,       BorderLayout.CENTER);
        centerPanel.add(lblWarningPembayaran, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.add(btnKembali);
        bottomPanel.add(btnBayar);
        add(bottomPanel, BorderLayout.SOUTH);

        btnCekVoucher.addActionListener(e -> {
            String kode = tfVoucher.getText().trim();
            if (kode.isEmpty()) { voucherAktif = null; diskon = 0; hitungUlangTotal(saldoPelanggan); return; }
            Voucher v = txDAO.getVoucherByKode(kode);
            if (v == null) {
                JOptionPane.showMessageDialog(this, "Kode voucher tidak ditemukan!");
                voucherAktif = null; diskon = 0;
            } else if (v.getKuotaPemakaian() <= 0) {
                JOptionPane.showMessageDialog(this, "Kuota voucher sudah habis!");
                voucherAktif = null; diskon = 0;
            } else if (subtotal < v.getMinimumBelanja()) {
                JOptionPane.showMessageDialog(this,
                    "Minimum belanja: " + formatRupiah(v.getMinimumBelanja()));
                voucherAktif = null; diskon = 0;
            } else {
                voucherAktif = v;
                diskon = v.getTipeDiskon().equalsIgnoreCase("PERSENTASE")
                    ? subtotal * (v.getNilaiDiskon() / 100.0)
                    : v.getNilaiDiskon();
                diskon = Math.min(diskon, subtotal);
                JOptionPane.showMessageDialog(this, "Voucher valid! Diskon: " + formatRupiah(diskon));
            }
            hitungUlangTotal(saldoPelanggan);
        });

        btnBayar.addActionListener(e -> prosesCheckout());

        revalidate();
        repaint();
    }

    private void hitungUlangTotal(double saldoPelanggan) {
        double total     = subtotal + ONGKIR_BASE - diskon;
        double sisaSaldo = saldoPelanggan - total;
        lblDiskon.setText(formatRupiah(diskon));
        lblTotal.setText(formatRupiah(total));
        lblSisaSaldo.setText(formatRupiah(sisaSaldo));
        lblSisaSaldo.setForeground(sisaSaldo >= 0 ? new Color(40, 167, 69) : Color.RED);
        lblWarningPembayaran.setText(saldoPelanggan < total
            ? "[!] Saldo tidak cukup! Silakan top up terlebih dahulu."
            : "[OK] Saldo mencukupi. Siap checkout!");
        lblWarningPembayaran.setForeground(saldoPelanggan < total ? Color.RED : new Color(40, 167, 69));
    }


    private void prosesCheckout() {
        if (listAlamat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tambahkan alamat pengiriman terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (listKurir.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Data kurir tidak tersedia!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (idMetodeSaldoZalora == -1) {
            JOptionPane.showMessageDialog(this, "Konfigurasi metode 'Saldo Zalora' tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int    idxKurir    = cbKurir.getSelectedIndex();
        int    idxAlamat   = cbAlamat.getSelectedIndex();
        int    idKurir     = (int)    listKurir.get(idxKurir)[0];
        String labelAlamat = (String) listAlamat.get(idxAlamat)[0];
        double total       = subtotal + ONGKIR_BASE - diskon;

        double saldoSaatIni = saldoDAO.getSaldoPelanggan(SessionManager.getIdPelanggan());
        if (saldoSaatIni < total) {
            JOptionPane.showMessageDialog(this,
                String.format("Saldo tidak cukup!\n\nSaldo: %s\nTagihan: %s\nKekurangan: %s",
                    formatRupiah(saldoSaatIni), formatRupiah(total), formatRupiah(total - saldoSaatIni)),
                "Saldo Tidak Cukup", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int konfirm = JOptionPane.showConfirmDialog(this,
            String.format("Konfirmasi Pembayaran:\n\nTotal: %s\nSaldo: %s\nSisa: %s\n\nLanjutkan?",
                formatRupiah(total), formatRupiah(saldoSaatIni), formatRupiah(saldoSaatIni - total)),
            "Konfirmasi Checkout", JOptionPane.YES_NO_OPTION);
        if (konfirm != JOptionPane.YES_OPTION) return;

        int idPesanan = orderDAO.getNextIdPesanan();

        boolean berhasil = frontendDAO.prosesCheckoutViaSP(
            idPesanan, SessionManager.getIdPelanggan(),
            idKurir, idMetodeSaldoZalora, labelAlamat,
            subtotal, ONGKIR_BASE, total,
            voucherAktif != null ? voucherAktif.getKodeVoucher() : null,
            keranjangItems);

        if (berhasil) {
            JOptionPane.showMessageDialog(this,
                String.format("Pembayaran berhasil!\n\nPesanan #%d sedang Diproses.\nSaldo berkurang %s.",
                    idPesanan, formatRupiah(total)),
                "Transaksi Berhasil", JOptionPane.INFORMATION_MESSAGE);
            parentFrame.setelahCheckoutBerhasil();
        } else {
            JOptionPane.showMessageDialog(this,
                "Transaksi gagal!\n\nKemungkinan penyebab:\n• Saldo tidak mencukupi\n• Stok produk habis\n• Koneksi database bermasalah",
                "Transaksi Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatRupiah(double nilai) {
        return String.format("Rp %,.0f", nilai);
    }
}
