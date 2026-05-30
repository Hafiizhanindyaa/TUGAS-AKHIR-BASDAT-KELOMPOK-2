package model;

import java.util.Date;

public class Ulasan {
    private int idUlasan;
    private int idPelanggan;
    private String skuProduk;
    private Date tanggalUlasan;
    private int rating;

    public Ulasan() {}

    public Ulasan(int idUlasan, int idPelanggan, String skuProduk, Date tanggalUlasan, int rating) {
        this.idUlasan = idUlasan;
        this.idPelanggan = idPelanggan;
        this.skuProduk = skuProduk;
        this.tanggalUlasan = tanggalUlasan;
        this.rating = rating;
    }

        public int getIdUlasan() { return idUlasan; }
    public void setIdUlasan(int idUlasan) { this.idUlasan = idUlasan; }

    public int getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(int idPelanggan) { this.idPelanggan = idPelanggan; }

    public String getSkuProduk() { return skuProduk; }
    public void setSkuProduk(String skuProduk) { this.skuProduk = skuProduk; }

    public Date getTanggalUlasan() { return tanggalUlasan; }
    public void setTanggalUlasan(Date tanggalUlasan) { this.tanggalUlasan = tanggalUlasan; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}