package model;

public class Aksesoris extends Produk {
    private String jenisAksesoris;
    private String materialPembuat;

    public Aksesoris() {}

        public Aksesoris(String skuProduk, String namaProduk, String deskripsi, String merek, double hargaJual, int idKategori, String jenisAksesoris, String materialPembuat) {
        setSkuProduk(skuProduk);
        setNamaProduk(namaProduk);
        setDeskripsi(deskripsi);
        setMerek(merek);
        setHargaJual(hargaJual);
        setIdKategori(idKategori);
        this.jenisAksesoris = jenisAksesoris;
        this.materialPembuat = materialPembuat;
    }

        public String getJenisAksesoris() { return jenisAksesoris; }
    public void setJenisAksesoris(String jenisAksesoris) { this.jenisAksesoris = jenisAksesoris; }

    public String getMaterialPembuat() { return materialPembuat; }
    public void setMaterialPembuat(String materialPembuat) { this.materialPembuat = materialPembuat; }
}