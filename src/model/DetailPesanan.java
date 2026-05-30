package model;

public class DetailPesanan {
    private int idPesanan;
    private int idVarian;
    private double subtotal;
    private int jumlahBeli;
    private double hargaSaatBeli;

    public DetailPesanan() {}

    public DetailPesanan(int idPesanan, int idVarian, double subtotal, int jumlahBeli, double hargaSaatBeli) {
        this.idPesanan = idPesanan;
        this.idVarian = idVarian;
        this.subtotal = subtotal;
        this.jumlahBeli = jumlahBeli;
        this.hargaSaatBeli = hargaSaatBeli;
    }

    // Getter dan Setter
    public int getIdPesanan() { return idPesanan; }
    public void setIdPesanan(int idPesanan) { this.idPesanan = idPesanan; }

    public int getIdVarian() { return idVarian; }
    public void setIdVarian(int idVarian) { this.idVarian = idVarian; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public int getJumlahBeli() { return jumlahBeli; }
    public void setJumlahBeli(int jumlahBeli) { this.jumlahBeli = jumlahBeli; }

    public double getHargaSaatBeli() { return hargaSaatBeli; }
    public void setHargaSaatBeli(double hargaSaatBeli) { this.hargaSaatBeli = hargaSaatBeli; }
}