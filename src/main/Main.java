package main;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Menjalankan GUI di thread yang aman (Event Dispatch Thread) tanpa UIManager OS
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Membuka jendela pilihan akses utama saat pertama kali aplikasi dijalankan
                RoleSelectionFrame frameUtama = new RoleSelectionFrame();
                frameUtama.setVisible(true);
            }
        });
    }
}
