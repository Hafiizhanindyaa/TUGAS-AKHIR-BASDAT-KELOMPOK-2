package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import frontend.CustomerDashboardFrame;
import backend.AdminDashboardFrame;

public class RoleSelectionFrame extends JFrame {

    public RoleSelectionFrame() {
        setTitle("Zalora E-Commerce System - Kelompok 2");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Sistem Manajemen E-Commerce Zalora", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(25, 10, 10, 10));
        add(lblTitle, BorderLayout.NORTH);

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


        btnCustomer.addActionListener(e -> {
            LoginDialog loginDlg = new LoginDialog(this, "PEMBELI");
            loginDlg.setVisible(true);

            if (loginDlg.isLoginSuccess()) {
                JOptionPane.showMessageDialog(this, "Selamat Datang, " + config.SessionManager.getNamaAkun() + "!");
                                new CustomerDashboardFrame().setVisible(true);
                this.dispose();
            }
        });

        btnAdmin.addActionListener(e -> {
            LoginDialog loginDlg = new LoginDialog(this, "ADMIN");
            loginDlg.setVisible(true);

            if (loginDlg.isLoginSuccess()) {
                JOptionPane.showMessageDialog(this, "Akses Admin Diterima. Membuka Back-End Panel...");
                                new AdminDashboardFrame().setVisible(true);
                this.dispose();
            }
        });
    }
}