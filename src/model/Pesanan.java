package model;

import java.util.Date;

public class Pesanan {
    private int idPesanan;
    private Date waktuTransaksi;
    private double totalHargaBarang;
    private double biayaPengiriman;
    private double totalTagihan;
    private String statusPesanan;
    private String labelAlamat;
    private int idPelanggan;
    private int idMetode;
    private int idKurir;
    private String kodeVoucher; // Bisa berharga null di DB

    public Pesanan() {}

    public Pesanan(int idPesanan, Date waktuTransaksi, double totalHargaBarang, double biayaPengiriman, double totalTagihan, String statusPesanan, String labelAlamat, int idPelanggan, int idMetode, int idKurir, String kodeVoucher) {
        this.idPesanan = idPesanan;
        this.waktuTransaksi = waktuTransaksi;
        this.totalHargaBarang = totalHargaBarang;
        this.biayaPengiriman = biayaPengiriman;
        this.totalTagihan = totalTagihan;
        this.statusPesanan = statusPesanan;
        this.labelAlamat = labelAlamat;
        this.idPelanggan = idPelanggan;
        this.idMetode = idMetode;
        this.idKurir = idKurir;
        this.kodeVoucher = kodeVoucher;
    }

    // Getter dan Setter
    public int getIdPesanan() { return idPesanan; }
    public void setIdPesanan(int idPesanan) { this.idPesanan = idPesanan; }

    public Date getWaktuTransaksi() { return waktuTransaksi; }
    public void setWaktuTransaksi(Date waktuTransaksi) { this.waktuTransaksi = waktuTransaksi; }

    public double getTotalHargaBarang() { return totalHargaBarang; }
    public void setTotalHargaBarang(double totalHargaBarang) { this.totalHargaBarang = totalHargaBarang; }

    public double getBiayaPengiriman() { return biayaPengiriman; }
    public void setBiayaPengiriman(double biayaPengiriman) { this.biayaPengiriman = biayaPengiriman; }

    public double getTotalTagihan() { return totalTagihan; }
    public void setTotalTagihan(double totalTagihan) { this.totalTagihan = totalTagihan; }

    public String getStatusPesanan() { return statusPesanan; }
    public void setStatusPesanan(String statusPesanan) { this.statusPesanan = statusPesanan; }

    public String getLabelAlamat() { return labelAlamat; }
    public void setLabelAlamat(String labelAlamat) { this.labelAlamat = labelAlamat; }

    public int getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(int idPelanggan) { this.idPelanggan = idPelanggan; }

    public int getIdMetode() { return idMetode; }
    public void setIdMetode(int idMetode) { this.idMetode = idMetode; }

    public int getIdKurir() { return idKurir; }
    public void setIdKurir(int idKurir) { this.idKurir = idKurir; }

    public String getKodeVoucher() { return kodeVoucher; }
    public void setKodeVoucher(String kodeVoucher) { this.kodeVoucher = kodeVoucher; }
}