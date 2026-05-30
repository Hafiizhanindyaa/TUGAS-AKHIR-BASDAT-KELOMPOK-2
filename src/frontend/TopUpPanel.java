package frontend;

import config.SessionManager;
import dao.UserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TopUpPanel extends JPanel {

    private UserDAO userDAO;
    private JLabel lblSaldoNilai;
    private JTextField tfNominal;
    private JTable tblRiwayat;
    private DefaultTableModel modelRiwayat;
    private JButton btnTopUp, btnRefresh;

        private static final double NOMINAL_MINIMUM = 10_000;

    public TopUpPanel() {
        this.userDAO = new UserDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        buatUI();
        muatData();
    }

    private void buatUI() {
        JLabel lblTitle = new JLabel("Saldo & Top Up");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        JPanel panelTengah = new JPanel(new BorderLayout(10, 15));

                JPanel kartuSaldo = new JPanel(new BorderLayout(8, 4));
        kartuSaldo.setBackground(new Color(40, 167, 69));
        kartuSaldo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 130, 55), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblSaldoJudul = new JLabel("Saldo Zalora Anda");
        lblSaldoJudul.setForeground(Color.WHITE);
        lblSaldoJudul.setFont(new Font("Arial", Font.PLAIN, 13));

        lblSaldoNilai = new JLabel("Rp 0");
        lblSaldoNilai.setForeground(Color.WHITE);
        lblSaldoNilai.setFont(new Font("Arial", Font.BOLD, 26));

        JLabel lblInfo = new JLabel("Saldo dapat digunakan untuk berbelanja di Zalora");
        lblInfo.setForeground(new Color(200, 255, 200));
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));

        kartuSaldo.add(lblSaldoJudul, BorderLayout.NORTH);
        kartuSaldo.add(lblSaldoNilai, BorderLayout.CENTER);
        kartuSaldo.add(lblInfo, BorderLayout.SOUTH);

        JPanel panelTopUp = new JPanel(new GridBagLayout());
        panelTopUp.setBorder(BorderFactory.createTitledBorder("Isi Saldo (Top Up)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panelTopUp.add(new JLabel("Nominal Top Up (Rp):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        tfNominal = new JTextField();
        tfNominal.setFont(new Font("Arial", Font.PLAIN, 13));
        tfNominal.setToolTipText("Masukkan nominal minimal Rp 10.000");
        panelTopUp.add(tfNominal, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        JLabel lblMin = new JLabel("Minimal Rp 10.000");
        lblMin.setFont(new Font("Arial", Font.ITALIC, 11));
        lblMin.setForeground(Color.GRAY);
        panelTopUp.add(lblMin, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        btnTopUp = new JButton("Top Up Sekarang");
        btnTopUp.setBackground(new Color(0, 123, 255));
        btnTopUp.setForeground(Color.WHITE);
        btnTopUp.setFont(new Font("Arial", Font.BOLD, 13));
        btnTopUp.setFocusPainted(false);
        panelTopUp.add(btnTopUp, gbc);

        JPanel panelAtas = new JPanel(new BorderLayout(0, 12));
        panelAtas.add(kartuSaldo, BorderLayout.NORTH);
        panelAtas.add(panelTopUp, BorderLayout.CENTER);
        panelTengah.add(panelAtas, BorderLayout.NORTH);

        String[] kolom = {"No.", "Nominal Top Up", "Waktu"};
        modelRiwayat = new DefaultTableModel(kolom, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRiwayat = new JTable(modelRiwayat);
        tblRiwayat.setRowHeight(24);
        tblRiwayat.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblRiwayat.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblRiwayat.getColumnModel().getColumn(2).setPreferredWidth(200);

        JScrollPane scrollRiwayat = new JScrollPane(tblRiwayat);
        scrollRiwayat.setBorder(BorderFactory.createTitledBorder("Riwayat Top Up"));
        scrollRiwayat.setPreferredSize(new Dimension(0, 200));
        panelTengah.add(scrollRiwayat, BorderLayout.CENTER);

        add(panelTengah, BorderLayout.CENTER);

        btnRefresh = new JButton("Refresh");
        JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBawah.add(btnRefresh);
        add(panelBawah, BorderLayout.SOUTH);

        btnTopUp.addActionListener(e -> prosesTopUp());
        btnRefresh.addActionListener(e -> muatData());
    }


    private void prosesTopUp() {
        String inputNominal = tfNominal.getText().trim();

        if (inputNominal.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nominal top up tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            tfNominal.requestFocus();
            return;
        }

        double nominal;
        try {
                        String bersih = inputNominal.replace(".", "").replace(",", "");
            nominal = Double.parseDouble(bersih);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Nominal harus berupa angka!\nContoh: 50000 atau 100000", "Peringatan", JOptionPane.WARNING_MESSAGE);
            tfNominal.requestFocus();
            return;
        }

        if (nominal <= 0) {
            JOptionPane.showMessageDialog(this, "Nominal harus lebih dari Rp 0!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (nominal < NOMINAL_MINIMUM) {
            JOptionPane.showMessageDialog(this,
                String.format("Nominal minimum top up adalah Rp %,.0f!", NOMINAL_MINIMUM),
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            tfNominal.requestFocus();
            return;
        }

        int konfirm = JOptionPane.showConfirmDialog(this,
            String.format("Konfirmasi top up sebesar Rp %,.0f?\n\nSaldo akan bertambah setelah dikonfirmasi.", nominal),
            "Konfirmasi Top Up", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (konfirm != JOptionPane.YES_OPTION) return;

        int idPelanggan = SessionManager.getIdPelanggan();
        boolean berhasil = userDAO.topUpSaldo(idPelanggan, nominal);

        if (berhasil) {
            tfNominal.setText("");
            JOptionPane.showMessageDialog(this,
                String.format("Top up Rp %,.0f berhasil!\nSaldo Anda telah diperbarui.", nominal),
                "Top Up Berhasil", JOptionPane.INFORMATION_MESSAGE);
            muatData();
        } else {
            JOptionPane.showMessageDialog(this,
                "Top up gagal! Periksa koneksi database dan coba lagi.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void muatData() {
        int idPelanggan = SessionManager.getIdPelanggan();

        double saldo = userDAO.getSaldoPelanggan(idPelanggan);
        if (saldo < 0) {
            lblSaldoNilai.setText("Error: gagal memuat saldo");
        } else {
            lblSaldoNilai.setText(formatRupiah(saldo));
        }

        modelRiwayat.setRowCount(0);
        List<Object[]> riwayat = userDAO.getRiwayatTopUp(idPelanggan);
        if (riwayat.isEmpty()) {
            modelRiwayat.addRow(new Object[]{"", "Belum ada riwayat top up", ""});
        } else {
            int no = 1;
            for (Object[] baris : riwayat) {
                modelRiwayat.addRow(new Object[]{
                    no,
                    formatRupiah((double) baris[1]),
                    baris[2]
                });
                no++;
            }
        }
    }


    private String formatRupiah(double nilai) {
        return String.format("Rp %,.0f", nilai);
    }
}
