package model;

public class VarianProduk {
    private int idVarian;
    private String warna;
    private String ukuran;
    private int stok;
    private String skuProduk;

    public VarianProduk() {}

    public VarianProduk(int idVarian, String warna, String ukuran, int stok, String skuProduk) {
        this.idVarian = idVarian;
        this.warna = warna;
        this.ukuran = ukuran;
        this.stok = stok;
        this.skuProduk = skuProduk;
    }

        public int getIdVarian() { return idVarian; }
    public void setIdVarian(int idVarian) { this.idVarian = idVarian; }

    public String getWarna() { return warna; }
    public void setWarna(String warna) { this.warna = warna; }

    public String getUkuran() { return ukuran; }
    public void setUkuran(String ukuran) { this.ukuran = ukuran; }

    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }

    public String getSkuProduk() { return skuProduk; }
    public void setSkuProduk(String skuProduk) { this.skuProduk = skuProduk; }
}