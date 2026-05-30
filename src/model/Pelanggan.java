package model;

import java.util.Date;

public class Pelanggan {
    private int idPelanggan;
    private String namaAkun;
    private String surel;
    private String kataSandi;
    private Date tanggalLahir;

    public Pelanggan() {}

    public Pelanggan(int idPelanggan, String namaAkun, String surel, String kataSandi, Date tanggalLahir) {
        this.idPelanggan = idPelanggan;
        this.namaAkun = namaAkun;
        this.surel = surel;
        this.kataSandi = kataSandi;
        this.tanggalLahir = tanggalLahir;
    }

        public int getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(int idPelanggan) { this.idPelanggan = idPelanggan; }

    public String getNamaAkun() { return namaAkun; }
    public void setNamaAkun(String namaAkun) { this.namaAkun = namaAkun; }

    public String getSurel() { return surel; }
    public void setSurel(String surel) { this.surel = surel; }

    public String getKataSandi() { return kataSandi; }
    public void setKataSandi(String kataSandi) { this.kataSandi = kataSandi; }

    public Date getTanggalLahir() { return tanggalLahir; }
    public void setTanggalLahir(Date tanggalLahir) { this.tanggalLahir = tanggalLahir; }
}