package model;

public class PelangganNomorTeleponAktif {
    private String nomorTeleponAktif;
    private int idPelanggan;

    public PelangganNomorTeleponAktif() {}

    public PelangganNomorTeleponAktif(String nomorTeleponAktif, int idPelanggan) {
        this.nomorTeleponAktif = nomorTeleponAktif;
        this.idPelanggan = idPelanggan;
    }

        public String getNomorTeleponAktif() { return nomorTeleponAktif; }
    public void setNomorTeleponAktif(String nomorTeleponAktif) { this.nomorTeleponAktif = nomorTeleponAktif; }

    public int getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(int idPelanggan) { this.idPelanggan = idPelanggan; }
}