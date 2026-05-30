package model;

public class Kategori {
    private int idKategori;
    private String namaKategori;
    private String targetGender;

        public Kategori() {}

        public Kategori(int idKategori, String namaKategori, String targetGender) {
        this.idKategori = idKategori;
        this.namaKategori = namaKategori;
        this.targetGender = targetGender;
    }

        public int getIdKategori() { return idKategori; }
    public void setIdKategori(int idKategori) { this.idKategori = idKategori; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    public String getTargetGender() { return targetGender; }
    public void setTargetGender(String targetGender) { this.targetGender = targetGender; }
}