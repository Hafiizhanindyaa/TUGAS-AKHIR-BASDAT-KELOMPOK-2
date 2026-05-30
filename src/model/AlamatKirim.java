package model;

public class AlamatKirim {
    private String labelAlamat;
    private int idPelanggan;
    private String namaPenerima;
    private String noTelpPenerima;
    private String detailJalan;
    private String kota;
    private String provinsi;

    public AlamatKirim() {}

    public AlamatKirim(String labelAlamat, int idPelanggan, String namaPenerima, String noTelpPenerima, String detailJalan, String kota, String provinsi) {
        this.labelAlamat = labelAlamat;
        this.idPelanggan = idPelanggan;
        this.namaPenerima = namaPenerima;
        this.noTelpPenerima = noTelpPenerima;
        this.detailJalan = detailJalan;
        this.kota = kota;
        this.provinsi = provinsi;
    }

    // Getter dan Setter
    public String getLabelAlamat() { return labelAlamat; }
    public void setLabelAlamat(String labelAlamat) { this.labelAlamat = labelAlamat; }

    public int getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(int idPelanggan) { this.idPelanggan = idPelanggan; }

    public String getNamaPenerima() { return namaPenerima; }
    public void setNamaPenerima(String namaPenerima) { this.namaPenerima = namaPenerima; }

    public String getNoTelpPenerima() { return noTelpPenerima; }
    public void setNoTelpPenerima(String noTelpPenerima) { this.noTelpPenerima = noTelpPenerima; }

    public String getDetailJalan() { return detailJalan; }
    public void setDetailJalan(String detailJalan) { this.detailJalan = detailJalan; }

    public String getKota() { return kota; }
    public void setKota(String kota) { this.kota = kota; }

    public String getProvinsi() { return provinsi; }
    public void setProvinsi(String provinsi) { this.provinsi = provinsi; }
}