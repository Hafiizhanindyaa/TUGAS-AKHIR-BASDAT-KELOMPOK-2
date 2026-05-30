package frontend;

import config.SessionManager;
import dao.OrderDAO;
import dao.SaldoDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TopUpSaldoPanel extends JPanel {

    private SaldoDAO saldoDAO;
    private OrderDAO orderDAO;

        private JLabel lblSaldoNilai;

        private JComboBox<String> cbMetodePembayaran;
    private JTextField tfNominal;
    private JButton btnTopUp;
    private JButton btnRefresh;

        private JTable tblRiwayat;
    private DefaultTableModel mdlRiwayat;

        private List<Object[]> listMetode;

        private static final NumberFormat fmtRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public TopUpSaldoPanel() {
        this.saldoDAO = new SaldoDAO();
        this.orderDAO = new OrderDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        buatUI();
        loadData();
    }


    private void buatUI() {

        JLabel lblTitle = new JLabel("Saldo & Top Up Zalora");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        JPanel panelAtas = new JPanel(new GridLayout(1, 2, 15, 0));

                JPanel kartuSaldo = new JPanel(new BorderLayout(5, 5));
        kartuSaldo.setBorder(BorderFactory.createTitledBorder("Saldo Zalora Anda"));
        kartuSaldo.setBackground(new Color(240, 255, 240));

        JLabel lblNama = new JLabel("Akun: " + SessionManager.getNamaAkun(), SwingConstants.CENTER);
        lblNama.setFont(new Font("Arial", Font.PLAIN, 12));

        lblSaldoNilai = new JLabel("Rp -", SwingConstants.CENTER);
        lblSaldoNilai.setFont(new Font("Arial", Font.BOLD, 24));
        lblSaldoNilai.setForeground(new Color(40, 167, 69));

        JLabel lblKeterangan = new JLabel("*Saldo digunakan untuk checkout pesanan", SwingConstants.CENTER);
        lblKeterangan.setFont(new Font("Arial", Font.ITALIC, 11));
        lblKeterangan.setForeground(Color.GRAY);

        kartuSaldo.add(lblNama, BorderLayout.NORTH);
        kartuSaldo.add(lblSaldoNilai, BorderLayout.CENTER);
        kartuSaldo.add(lblKeterangan, BorderLayout.SOUTH);

        JPanel formTopUp = new JPanel(new GridLayout(5, 2, 8, 10));
        formTopUp.setBorder(BorderFactory.createTitledBorder("Form Top Up Saldo"));

        formTopUp.add(new JLabel("Metode Pembayaran:"));
        cbMetodePembayaran = new JComboBox<>();
        formTopUp.add(cbMetodePembayaran);

        formTopUp.add(new JLabel("Nominal Top Up (Rp):"));
        tfNominal = new JTextField();
        formTopUp.add(tfNominal);

        formTopUp.add(new JLabel("Min. top up:"));
        formTopUp.add(new JLabel("Rp 10.000"));

        formTopUp.add(new JLabel(""));
        formTopUp.add(new JLabel("*Metode pembayaran untuk keperluan top up saja"));

        btnTopUp = new JButton("TOP UP SEKARANG");
        btnTopUp.setBackground(new Color(0, 123, 255));
        btnTopUp.setForeground(Color.WHITE);
        btnTopUp.setFont(new Font("Arial", Font.BOLD, 13));
        formTopUp.add(new JLabel(""));
        formTopUp.add(btnTopUp);

        panelAtas.add(kartuSaldo);
        panelAtas.add(formTopUp);

        String[] kolomRiwayat = {
            "ID Mutasi", "Jenis", "Nominal", "Saldo Sebelum", "Saldo Sesudah", "Waktu", "Keterangan", "Metode"
        };
        mdlRiwayat = new DefaultTableModel(kolomRiwayat, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRiwayat = new JTable(mdlRiwayat);
        tblRiwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRiwayat.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblRiwayat.getColumnModel().getColumn(1).setPreferredWidth(80);
        tblRiwayat.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblRiwayat.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblRiwayat.getColumnModel().getColumn(4).setPreferredWidth(110);
        tblRiwayat.getColumnModel().getColumn(5).setPreferredWidth(140);
        tblRiwayat.getColumnModel().getColumn(6).setPreferredWidth(240);
        tblRiwayat.getColumnModel().getColumn(7).setPreferredWidth(130);

        JScrollPane scrollRiwayat = new JScrollPane(tblRiwayat);
        scrollRiwayat.setBorder(BorderFactory.createTitledBorder("Riwayat Mutasi Saldo"));
        scrollRiwayat.setPreferredSize(new Dimension(0, 220));

        btnRefresh = new JButton("Refresh");
        JPanel panelRefresh = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelRefresh.add(btnRefresh);

        JPanel panelBawah = new JPanel(new BorderLayout(5, 5));
        panelBawah.add(scrollRiwayat, BorderLayout.CENTER);
        panelBawah.add(panelRefresh, BorderLayout.SOUTH);

        JPanel panelCenter = new JPanel(new BorderLayout(10, 10));
        panelCenter.add(panelAtas, BorderLayout.NORTH);
        panelCenter.add(panelBawah, BorderLayout.CENTER);
        add(panelCenter, BorderLayout.CENTER);

        btnTopUp.addActionListener(e -> prosesTopUp());
        btnRefresh.addActionListener(e -> loadData());
    }


    public void loadData() {
        loadSaldo();
        loadMetodePembayaran();
        loadRiwayatSaldo();
    }


    private void loadSaldo() {
        double saldo = saldoDAO.getSaldoPelanggan(SessionManager.getIdPelanggan());
        if (saldo >= 0) {
            lblSaldoNilai.setText(formatRupiah(saldo));
        } else {
            lblSaldoNilai.setText("Gagal memuat saldo");
            lblSaldoNilai.setForeground(Color.RED);
        }
    }


    private void loadMetodePembayaran() {
        cbMetodePembayaran.removeAllItems();
        listMetode = orderDAO.getMetodePembayaranUntukTopUp();
        if (listMetode.isEmpty()) {
            cbMetodePembayaran.addItem("-- Tidak ada metode pembayaran --");
        } else {
            for (Object[] metode : listMetode) {
                cbMetodePembayaran.addItem(metode[1].toString());
            }
        }
    }


    private void loadRiwayatSaldo() {
        mdlRiwayat.setRowCount(0);
        List<Object[]> riwayat = saldoDAO.getRiwayatSaldo(SessionManager.getIdPelanggan());
        for (Object[] baris : riwayat) {
                        String jenis         = baris[1].toString();
            double nominal       = (double) baris[2];
            double saldoSblm     = (double) baris[3];
            double saldoSsdh     = (double) baris[4];
            String waktu         = baris[5].toString();
            String keterangan    = baris[6] != null ? baris[6].toString() : "-";
            String namaMetode    = baris[8] != null ? baris[8].toString() : "-";

                        mdlRiwayat.addRow(new Object[]{
                baris[0],
                jenis,
                formatRupiah(nominal),
                formatRupiah(saldoSblm),
                formatRupiah(saldoSsdh),
                waktu,
                keterangan,
                namaMetode
            });
        }
    }


    private void prosesTopUp() {
        if (listMetode == null || listMetode.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Tidak ada metode pembayaran yang tersedia!\nHubungi admin.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idxMetode = cbMetodePembayaran.getSelectedIndex();
        if (idxMetode < 0 || idxMetode >= listMetode.size()) {
            JOptionPane.showMessageDialog(this,
                "Pilih metode pembayaran top up terlebih dahulu!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String inputNominal = tfNominal.getText().trim();
        if (inputNominal.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Nominal top up tidak boleh kosong!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double nominal;
        try {
                        inputNominal = inputNominal.replace(".", "").replace(",", "").replace(" ", "");
            nominal = Double.parseDouble(inputNominal);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Nominal harus berupa angka!\nContoh: 50000",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (nominal <= 0) {
            JOptionPane.showMessageDialog(this,
                "Nominal top up harus lebih dari 0!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (nominal < 10000) {
            JOptionPane.showMessageDialog(this,
                "Nominal minimum top up adalah Rp 10.000!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object[] metodeTerpilih = listMetode.get(idxMetode);
        int    idMetode    = (int)    metodeTerpilih[0];
        String namaMetode  = (String) metodeTerpilih[2];
        String labelMetode = (String) metodeTerpilih[1];

        int konfirm = JOptionPane.showConfirmDialog(this,
            String.format("Konfirmasi Top Up Saldo Zalora:\n\n"
                        + "Metode  : %s\n"
                        + "Nominal : %s\n\n"
                        + "Lanjutkan?", labelMetode, formatRupiah(nominal)),
            "Konfirmasi Top Up", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (konfirm != JOptionPane.YES_OPTION) return;

        boolean berhasil = saldoDAO.topUpSaldo(
            SessionManager.getIdPelanggan(),
            nominal,
            idMetode,
            namaMetode
        );

        if (berhasil) {
            JOptionPane.showMessageDialog(this,
                "Top Up berhasil!\n"
                + "Saldo Zalora Anda bertambah " + formatRupiah(nominal) + ".\n"
                + "Saldo terbaru akan ditampilkan di bawah.",
                "Top Up Berhasil", JOptionPane.INFORMATION_MESSAGE);
            tfNominal.setText("");
            loadData();
        } else {
            JOptionPane.showMessageDialog(this,
                "Top Up gagal! Periksa koneksi ke database SQL Server.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private String formatRupiah(double nilai) {
        return String.format("Rp %,.0f", nilai);
    }
}
