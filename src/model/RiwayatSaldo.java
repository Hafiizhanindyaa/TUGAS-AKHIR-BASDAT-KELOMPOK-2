package model;

import java.util.Date;

public class RiwayatSaldo {
    private int idMutasi;
    private int idPelanggan;
    private String jenisMutasi;
    private double nominal;
    private double saldoSebelum;
    private double saldoSesudah;
    private Date waktuMutasi;
    private String keterangan;
    private Integer idPesanan;
    private Integer idMetode;

    public RiwayatSaldo() {}

    public RiwayatSaldo(int idMutasi, int idPelanggan, String jenisMutasi,
                        double nominal, double saldoSebelum, double saldoSesudah,
                        Date waktuMutasi, String keterangan,
                        Integer idPesanan, Integer idMetode) {
        this.idMutasi     = idMutasi;
        this.idPelanggan  = idPelanggan;
        this.jenisMutasi  = jenisMutasi;
        this.nominal      = nominal;
        this.saldoSebelum = saldoSebelum;
        this.saldoSesudah = saldoSesudah;
        this.waktuMutasi  = waktuMutasi;
        this.keterangan   = keterangan;
        this.idPesanan    = idPesanan;
        this.idMetode     = idMetode;
    }

        public int getIdMutasi()                 { return idMutasi; }
    public void setIdMutasi(int v)           { this.idMutasi = v; }

    public int getIdPelanggan()              { return idPelanggan; }
    public void setIdPelanggan(int v)        { this.idPelanggan = v; }

    public String getJenisMutasi()           { return jenisMutasi; }
    public void setJenisMutasi(String v)     { this.jenisMutasi = v; }

    public double getNominal()               { return nominal; }
    public void setNominal(double v)         { this.nominal = v; }

    public double getSaldoSebelum()          { return saldoSebelum; }
    public void setSaldoSebelum(double v)    { this.saldoSebelum = v; }

    public double getSaldoSesudah()          { return saldoSesudah; }
    public void setSaldoSesudah(double v)    { this.saldoSesudah = v; }

    public Date getWaktuMutasi()             { return waktuMutasi; }
    public void setWaktuMutasi(Date v)       { this.waktuMutasi = v; }

    public String getKeterangan()            { return keterangan; }
    public void setKeterangan(String v)      { this.keterangan = v; }

    public Integer getIdPesanan()            { return idPesanan; }
    public void setIdPesanan(Integer v)      { this.idPesanan = v; }

    public Integer getIdMetode()             { return idMetode; }
    public void setIdMetode(Integer v)       { this.idMetode = v; }
}
