package main;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private boolean succeeded;

    public LoginDialog(Frame parent) {
        super(parent, "Login Admin - Proteksi Sistem", true); // true = Modal
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel panelForm = new JPanel(new GridLayout(2, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        panelForm.add(new JLabel("Username:"));
        tfUsername = new JTextField();
        panelForm.add(tfUsername);

        panelForm.add(new JLabel("Password:"));
        pfPassword = new JPasswordField();
        panelForm.add(pfPassword);

        add(panelForm, BorderLayout.CENTER);

        // Panel Tombol Aksi
        JPanel panelActions = new JPanel();
        JButton btnLogin = new JButton("Login");
        JButton btnCancel = new JButton("Batal");
        panelActions.add(btnLogin);
        panelActions.add(btnCancel);
        add(panelActions, BorderLayout.SOUTH);

        // --- LOGIKA TOMBOL LOGIN ---
        btnLogin.addActionListener(e -> {
            String username = tfUsername.getText();
            String password = new String(pfPassword.getPassword());

            // CONTOH HARDCODE: Kamu bisa ganti pakai query ke database kalau mau dinamis
            if (username.equals("admin") && password.equals("zalora2")) {
                succeeded = true;
                dispose(); // Tutup pop up login
            } else {
                JOptionPane.showMessageDialog(this,
                        "Username atau Password salah!",
                        "Login Gagal",
                        JOptionPane.ERROR_MESSAGE);
                succeeded = false;
            }
        });

        btnCancel.addActionListener(e -> {
            succeeded = false;
            dispose();
        });
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}