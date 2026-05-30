package backend;

import config.SessionManager;
import main.RoleSelectionFrame;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardFrame extends JFrame {
    private JPanel panelContentRight;
    private CardLayout cardLayout;

    public AdminDashboardFrame() {
        setTitle("Zalora Back-End Admin Panel - Kelompok 2");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

                                JPanel panelSidebar = new JPanel();
        panelSidebar.setBackground(new Color(23, 162, 184));
        panelSidebar.setPreferredSize(new Dimension(250, 720));
        panelSidebar.setLayout(new BoxLayout(panelSidebar, BoxLayout.Y_AXIS));

        JLabel lblAdmin = new JLabel("PANEL KONTROL ADMIN");
        lblAdmin.setForeground(Color.WHITE);
        lblAdmin.setFont(new Font("Arial", Font.BOLD, 15));
        lblAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAdmin.setBorder(BorderFactory.createEmptyBorder(25, 10, 20, 10));
        panelSidebar.add(lblAdmin);

        JLabel lblSeksiCrud = buatLabelSeksi("── MANAJEMEN DATA ──");
        panelSidebar.add(lblSeksiCrud);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 6)));

        JButton btnInventory   = createAdminNavButton("Kelola Inventory (CRUD)");
        JButton btnOperational = createAdminNavButton("Kelola Operasional (CRUD)");
        JButton btnPromo       = createAdminNavButton("Kelola Voucher Promo");
        JButton btnOrders      = createAdminNavButton("Pantau Pesanan Masuk");

        panelSidebar.add(btnInventory);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        panelSidebar.add(btnOperational);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        panelSidebar.add(btnPromo);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        panelSidebar.add(btnOrders);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 18)));

        JLabel lblSeksiAnalisis = buatLabelSeksi("── ANALISIS DATA ──");
        panelSidebar.add(lblSeksiAnalisis);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 6)));

        JButton btnTopProduk    = createAnalisisNavButton(
            "TOP 5 Produk Terlaris",
            "via VIEW SQL Server",
            new Color(23, 162, 184));

                JButton btnTopPelanggan = createAnalisisNavButton(
            "TOP 5 Pembeli Terbanyak",
            "via Stored Procedure",
            new Color(40, 167, 69));

                JButton btnProdukBareng = createAnalisisNavButton(
            "Produk Dibeli Bersamaan",
            "via Self-Join Subquery",
            new Color(111, 66, 193));

        panelSidebar.add(btnTopProduk);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        panelSidebar.add(btnTopPelanggan);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        panelSidebar.add(btnProdukBareng);

                panelSidebar.add(Box.createVerticalGlue());
        JButton btnLogout = createAdminNavButton("Logout dari Sistem");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        panelSidebar.add(btnLogout);
        panelSidebar.add(Box.createRigidArea(new Dimension(0, 25)));

        add(panelSidebar, BorderLayout.WEST);

                                cardLayout = new CardLayout();
        panelContentRight = new JPanel(cardLayout);

                panelContentRight.add(new InventoryManagementPanel(),   "INVENTORY");
        panelContentRight.add(new OperationalManagementPanel(), "OPERASIONAL");
        panelContentRight.add(new PromoManagementPanel(),       "PROMO");
        panelContentRight.add(new OrderManagementPanel(),       "PESANAN");

                panelContentRight.add(new TopProdukPanel(),       "TOP_PRODUK");
        panelContentRight.add(new TopPelangganPanel(),    "TOP_PELANGGAN");
        panelContentRight.add(new ProdukBersamaanPanel(), "PRODUK_BARENG");

        add(panelContentRight, BorderLayout.CENTER);

                                btnInventory.addActionListener(e   -> cardLayout.show(panelContentRight, "INVENTORY"));
        btnOperational.addActionListener(e -> cardLayout.show(panelContentRight, "OPERASIONAL"));
        btnPromo.addActionListener(e       -> cardLayout.show(panelContentRight, "PROMO"));
        btnOrders.addActionListener(e      -> cardLayout.show(panelContentRight, "PESANAN"));

        btnTopProduk.addActionListener(e    -> cardLayout.show(panelContentRight, "TOP_PRODUK"));
        btnTopPelanggan.addActionListener(e -> cardLayout.show(panelContentRight, "TOP_PELANGGAN"));
        btnProdukBareng.addActionListener(e -> cardLayout.show(panelContentRight, "PRODUK_BARENG"));

        btnLogout.addActionListener(e -> {
            int opsi = JOptionPane.showConfirmDialog(this,
                "Keluar dari sistem administrasi?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (opsi == JOptionPane.YES_OPTION) {
                SessionManager.logout();
                new RoleSelectionFrame().setVisible(true);
                this.dispose();
            }
        });
    }

        private JButton createAdminNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        return btn;
    }


    private JButton createAnalisisNavButton(String label, String subLabel, Color warna) {
        String html = "<html><center><b>" + label + "</b><br>" +
                      "<font size='1' color='#ddeeff'><i>" + subLabel + "</i></font></center></html>";
        JButton btn = new JButton(html);
        btn.setMaximumSize(new Dimension(220, 52));
        btn.setPreferredSize(new Dimension(220, 52));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBackground(warna.darker());
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(warna.brighter(), 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        return btn;
    }

        private JLabel buatLabelSeksi(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(new Color(180, 230, 240));
        lbl.setFont(new Font("Arial", Font.BOLD, 10));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 5, 4, 5));
        lbl.setMaximumSize(new Dimension(230, 24));
        return lbl;
    }
}
