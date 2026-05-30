package model;

import java.util.Date;

public class Voucher {
    private String kodeVoucher;
    private String tipeDiskon;
    private double nilaiDiskon;
    private double minimumBelanja;
    private Date tanggalMulai;
    private Date tanggalBerakhir;
    private int kuotaPemakaian;

    public Voucher() {}

    public Voucher(String kodeVoucher, String tipeDiskon, double nilaiDiskon, double minimumBelanja, Date tanggalMulai, Date tanggalBerakhir, int kuotaPemakaian) {
        this.kodeVoucher = kodeVoucher;
        this.tipeDiskon = tipeDiskon;
        this.nilaiDiskon = nilaiDiskon;
        this.minimumBelanja = minimumBelanja;
        this.tanggalMulai = tanggalMulai;
        this.tanggalBerakhir = tanggalBerakhir;
        this.kuotaPemakaian = kuotaPemakaian;
    }

        public String getKodeVoucher() { return kodeVoucher; }
    public void setKodeVoucher(String kodeVoucher) { this.kodeVoucher = kodeVoucher; }

    public String getTipeDiskon() { return tipeDiskon; }
    public void setTipeDiskon(String tipeDiskon) { this.tipeDiskon = tipeDiskon; }

    public double getNilaiDiskon() { return nilaiDiskon; }
    public void setNilaiDiskon(double nilaiDiskon) { this.nilaiDiskon = nilaiDiskon; }

    public double getMinimumBelanja() { return minimumBelanja; }
    public void setMinimumBelanja(double minimumBelanja) { this.minimumBelanja = minimumBelanja; }

    public Date getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(Date tanggalMulai) { this.tanggalMulai = tanggalMulai; }

    public Date getTanggalBerakhir() { return tanggalBerakhir; }
    public void setTanggalBerakhir(Date tanggalBerakhir) { this.tanggalBerakhir = tanggalBerakhir; }

    public int getKuotaPemakaian() { return kuotaPemakaian; }
    public void setKuotaPemakaian(int kuotaPemakaian) { this.kuotaPemakaian = kuotaPemakaian; }
}