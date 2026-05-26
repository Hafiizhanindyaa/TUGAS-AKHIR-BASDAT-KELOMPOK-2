package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RoleSelectionFrame extends JFrame {
    
    public RoleSelectionFrame() {
        setTitle("Zalora E-Commerce System - Kelompok 2");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JLabel lblTitle = new JLabel("Sistem Manajemen E-Commerce Zalora", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(25, 10, 10, 10));
        add(lblTitle, BorderLayout.NORTH);

        // Panel Tombol
        JPanel panelButtons = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JButton btnCustomer = new JButton("Masuk sebagai Pembeli (Front-End)");
        btnCustomer.setPreferredSize(new Dimension(250, 45));
        gbc.gridx = 0; gbc.gridy = 0;
        panelButtons.add(btnCustomer, gbc);

        JButton btnAdmin = new JButton("Masuk sebagai Admin (Back-End)");
        btnAdmin.setPreferredSize(new Dimension(250, 45));
        gbc.gridx = 0; gbc.gridy = 1;
        panelButtons.add(btnAdmin, gbc);

        add(panelButtons, BorderLayout.CENTER);

        // --- ACTION LOGIC ---
        
        // Klik Pembeli -> Langsung buka dashboard pembeli
        btnCustomer.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Membuka Katalog Zalora...");
            // next: new CustomerDashboardFrame().setVisible(true);
            // this.dispose();
        });

        // Klik Admin -> Panggil Pop-up Login khusus Admin
        btnAdmin.addActionListener(e -> {
            LoginDialog loginDlg = new LoginDialog(this);
            loginDlg.setVisible(true);
            
            // Jika login sukses (diatur dari variabel di LoginDialog)
            if (loginDlg.isSucceeded()) {
                JOptionPane.showMessageDialog(this, "Login Berhasil! Selamat Datang Admin.");
                // next: new AdminDashboardFrame().setVisible(true);
                // this.dispose();
            }
        });
    }
}