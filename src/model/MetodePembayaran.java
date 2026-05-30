package model;

public class MetodePembayaran {
    private int idMetode;
    private String namaMetode;
    private String tipeMetode;
    private String namaProvider;

    public MetodePembayaran() {}

    public MetodePembayaran(int idMetode, String namaMetode, String tipeMetode, String namaProvider) {
        this.idMetode = idMetode;
        this.namaMetode = namaMetode;
        this.tipeMetode = tipeMetode;
        this.namaProvider = namaProvider;
    }

        public int getIdMetode() { return idMetode; }
    public void setIdMetode(int idMetode) { this.idMetode = idMetode; }

    public String getNamaMetode() { return namaMetode; }
    public void setNamaMetode(String namaMetode) { this.namaMetode = namaMetode; }

    public String getTipeMetode() { return tipeMetode; }
    public void setTipeMetode(String tipeMetode) { this.tipeMetode = tipeMetode; }

    public String getNamaProvider() { return namaProvider; }
    public void setNamaProvider(String namaProvider) { this.namaProvider = namaProvider; }
}