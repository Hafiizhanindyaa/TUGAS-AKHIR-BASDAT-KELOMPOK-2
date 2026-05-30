package model;

public class Kurir {
    private int idKurir;
    private String namaEkspedisi;
    private String jenisLayanan;
    private String estimasiWaktu;

    public Kurir() {}

    public Kurir(int idKurir, String namaEkspedisi, String jenisLayanan, String estimasiWaktu) {
        this.idKurir = idKurir;
        this.namaEkspedisi = namaEkspedisi;
        this.jenisLayanan = jenisLayanan;
        this.estimasiWaktu = estimasiWaktu;
    }

        public int getIdKurir() { return idKurir; }
    public void setIdKurir(int idKurir) { this.idKurir = idKurir; }

    public String getNamaEkspedisi() { return namaEkspedisi; }
    public void setNamaEkspedisi(String namaEkspedisi) { this.namaEkspedisi = namaEkspedisi; }

    public String getJenisLayanan() { return jenisLayanan; }
    public void setJenisLayanan(String jenisLayanan) { this.jenisLayanan = jenisLayanan; }

    public String getEstimasiWaktu() { return estimasiWaktu; }
    public void setEstimasiWaktu(String estimasiWaktu) { this.estimasiWaktu = estimasiWaktu; }
}