package frontend;

import config.SessionManager;
import main.RoleSelectionFrame;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CustomerDashboardFrame extends JFrame {
    private JPanel panelContentRight;
    private CardLayout cardLayout;
    private CartPanel cartPanel;
    private CheckoutPanel checkoutPanel;
    private ProductSearchPanel productSearchPanel;
    private OrderHistoryPanel orderHistoryPanel;
    private TopUpSaldoPanel topUpSaldoPanel;
    private ProfilPanel profilPanel;
    private UlasanPanel ulasanPanel;

    public CustomerDashboardFrame() {
        setTitle("Zalora Front-End Store - " + SessionManager.getNamaAkun());
        setSize(1050, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===================== SIDEBAR =====================
        JPanel panelSidebar = new JPanel();
        panelSidebar.setBackground(new Color(33, 37, 41));
        panelSidebar.setPreferredSize(new Dimension(230, 700));
        panelSidebar.setLayout(new BoxLayout(panelSidebar, BoxLayout.Y_AXIS));

        JLabel lblUser = new JLabel("Halo, " + SessionManager.getNamaAkun());
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblUser.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        panelSidebar.add(lblUser);

        // Label ID pelanggan kecil
        JLabel lblId = new JLabel("ID Pelanggan: #" + SessionManager.getIdPelanggan());
        lblId.setForeground(new Color(150, 150, 150));
        lblId.setFont(new Font("Arial", Font.PLAIN, 11));
        lblId.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblId.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 10));
        panelSidebar.add(lblId);

        JButton btnKatalog   = createNavButton("🛍 Cari & Katalog Produk");
        JButton btnKeranjang = createNavButton("🛒 Keranjang Belanja");
        JButton btnSaldo     = createNavButton("💳 Saldo & Top Up"); 
        JButton btnProfil   = createNavButton("👤 Profil & Alamat");
        JButton btnUlasan   = createNavButton("⭐ Ulasan Produk"); // MENU BARU
        JButton btnRiwayat   = createNavButton("📋 Riwayat Pesanan");
        JButton btnLogout    = createNavButton("Keluar / Logout");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);

        panelSidebar.add(btnKatalog);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        panelSidebar.add(btnKeranjang);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        panelSidebar.add(btnSaldo);       // tambahkan ke sidebar
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        panelSidebar.add(btnProfil);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        panelSidebar.add(btnUlasan);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        panelSidebar.add(btnRiwayat);
        panelSidebar.add(Box.createVerticalGlue());
        panelSidebar.add(btnLogout);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        add(panelSidebar, BorderLayout.WEST);

        // ===================== CONTENT AREA =====================
        cardLayout = new CardLayout();
        panelContentRight = new JPanel(cardLayout);

        productSearchPanel = new ProductSearchPanel(this);
        cartPanel          = new CartPanel(this);
        checkoutPanel      = new CheckoutPanel(this);
        orderHistoryPanel  = new OrderHistoryPanel();
        topUpSaldoPanel    = new TopUpSaldoPanel(); 
        profilPanel        = new ProfilPanel();
        ulasanPanel        = new UlasanPanel();// PANEL BARU

        panelContentRight.add(productSearchPanel, "KATALOG");
        panelContentRight.add(cartPanel,          "KERANJANG");
        panelContentRight.add(checkoutPanel,      "CHECKOUT");
        panelContentRight.add(orderHistoryPanel,  "RIWAYAT");
        panelContentRight.add(topUpSaldoPanel,    "SALDO");
        panelContentRight.add(profilPanel, "PROFIL");
        panelContentRight.add(ulasanPanel, "ULASAN");   // daftarkan ke CardLayout
        add(panelContentRight, BorderLayout.CENTER);

        // ===================== NAVIGASI =====================
        btnKatalog.addActionListener(e -> cardLayout.show(panelContentRight, "KATALOG"));

        btnKeranjang.addActionListener(e -> {
            cartPanel.refreshTabel();
            cardLayout.show(panelContentRight, "KERANJANG");
        });

        // Klik Saldo & Top Up -> load data terbaru dari database lalu tampilkan
        btnSaldo.addActionListener(e -> {
            topUpSaldoPanel.loadData();
            cardLayout.show(panelContentRight, "SALDO");
        });

        btnProfil.addActionListener(e -> {
            profilPanel.muatData();
            cardLayout.show(panelContentRight, "PROFIL");
        });
 
        btnUlasan.addActionListener(e -> {
            ulasanPanel.muatData();
            cardLayout.show(panelContentRight, "ULASAN");
        });

        btnRiwayat.addActionListener(e -> cardLayout.show(panelContentRight, "RIWAYAT"));

        btnLogout.addActionListener(e -> {
            int opsi = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin keluar?", "Logout", JOptionPane.YES_NO_OPTION);
            if (opsi == JOptionPane.YES_OPTION) {
                SessionManager.logout();
                new RoleSelectionFrame().setVisible(true);
                this.dispose();
            }
        });
    }

    public CartPanel getCartPanel() { return cartPanel; }

    public void bukaCheckout(List<Object[]> items) {
        checkoutPanel.loadCheckout(items);
        cardLayout.show(panelContentRight, "CHECKOUT");
    }

    public void bukaKeranjang() {
        cartPanel.refreshTabel();
        cardLayout.show(panelContentRight, "KERANJANG");
    }

    /**
     * Dipanggil setelah checkout berhasil.
     * Kosongkan keranjang, pindah ke riwayat pesanan, dan refresh panel saldo
     * agar saldo yang berkurang langsung terlihat.
     */
    public void setelahCheckoutBerhasil() {
        cartPanel.getKeranjangItems().clear();
        cartPanel.refreshTabel();
        // Refresh data saldo di background agar sinkron
        topUpSaldoPanel.loadData();
        cardLayout.show(panelContentRight, "RIWAYAT");
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(210, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        return btn;
    }
}
