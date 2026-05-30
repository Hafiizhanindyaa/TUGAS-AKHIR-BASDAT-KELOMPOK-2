package config;

public class SessionManager {
    private static int idPelangganActive;
    private static String namaAkunActive;
    private static String roleActive;

    public static void startSession(int id, String nama, String role) {
        idPelangganActive = id;
        namaAkunActive = nama;
        roleActive = role;
    }

    public static int getIdPelanggan() { return idPelangganActive; }
    public static String getNamaAkun() { return namaAkunActive; }
    public static String getRole() { return roleActive; }

    public static void logout() {
        idPelangganActive = 0;
        namaAkunActive = null;
        roleActive = null;
    }
}