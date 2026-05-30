package model;

public class Produk {
    private String skuProduk;
    private String namaProduk;
    private String deskripsi;
    private String merek;
    private double hargaJual;
    private int idKategori;

    public java.lang.String getSkuProduk() { return skuProduk; }
    public void setSkuProduk(java.lang.String skuProduk) { this.skuProduk = skuProduk; }

    public java.lang.String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(java.lang.String namaProduk) { this.namaProduk = namaProduk; }

    public java.lang.String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(java.lang.String deskripsi) { this.deskripsi = deskripsi; }

    public java.lang.String getMerek() { return merek; }
    public void setMerek(java.lang.String merek) { this.merek = merek; }

    public double getHargaJual() { return hargaJual; }
    public void setHargaJual(double hargaJual) { this.hargaJual = hargaJual; }

    public int getIdKategori() { return idKategori; }
    public void setIdKategori(int idKategori) { this.idKategori = idKategori; }
}