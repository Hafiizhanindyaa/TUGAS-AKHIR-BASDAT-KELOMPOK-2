package model;

public class Pakaian extends Produk {
    private String jenisPotongan;
    private String panduanPerawatan;

    public Pakaian() {}

        public Pakaian(String skuProduk, String namaProduk, String deskripsi, String merek, double hargaJual, int idKategori, String jenisPotongan, String panduanPerawatan) {
        setSkuProduk(skuProduk);
        setNamaProduk(namaProduk);
        setDeskripsi(deskripsi);
        setMerek(merek);
        setHargaJual(hargaJual);
        setIdKategori(idKategori);
        this.jenisPotongan = jenisPotongan;
        this.panduanPerawatan = panduanPerawatan;
    }

        public String getJenisPotongan() { return jenisPotongan; }
    public void setJenisPotongan(String jenisPotongan) { this.jenisPotongan = jenisPotongan; }

    public String getPanduanPerawatan() { return panduanPerawatan; }
    public void setPanduanPerawatan(String panduanPerawatan) { this.panduanPerawatan = panduanPerawatan; }
}