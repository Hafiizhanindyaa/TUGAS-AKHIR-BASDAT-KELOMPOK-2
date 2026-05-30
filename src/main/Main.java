package main;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                                RoleSelectionFrame frameUtama = new RoleSelectionFrame();
                frameUtama.setVisible(true);
            }
        });
    }
}
