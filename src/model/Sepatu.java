package model;

public class Sepatu extends Produk {
    private String materialPembuat;
    private String jenisSepatu;

    public Sepatu() {}

        public Sepatu(String skuProduk, String namaProduk, String deskripsi, String merek, double hargaJual, int idKategori, String materialPembuat, String jenisSepatu) {
        setSkuProduk(skuProduk);
        setNamaProduk(namaProduk);
        setDeskripsi(deskripsi);
        setMerek(merek);
        setHargaJual(hargaJual);
        setIdKategori(idKategori);
        this.materialPembuat = materialPembuat;
        this.jenisSepatu = jenisSepatu;
    }

        public String getMaterialPembuat() { return materialPembuat; }
    public void setMaterialPembuat(String materialPembuat) { this.materialPembuat = materialPembuat; }

    public String getJenisSepatu() { return jenisSepatu; }
    public void setJenisSepatu(String jenisSepatu) { this.jenisSepatu = jenisSepatu; }
}