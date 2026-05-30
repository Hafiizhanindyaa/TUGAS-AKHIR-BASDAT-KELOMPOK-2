package dao;

import config.DBConnection;
import model.Kategori;
import model.Produk;
import model.Pakaian;
import model.Sepatu;
import model.Aksesoris;
import model.VarianProduk;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {


        public List<Produk> searchProdukKatalog(String keyword, Integer idKategoriFilter) {
        List<Produk> list = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT p.*, pk.Jenis_Potongan, pk.Panduan_Perawatan, ")
          .append("s.Material_Pembuat AS Mat_Sepatu, s.Jenis_Sepatu, ")
          .append("a.Jenis_Aksesoris, a.Material_Pembuat AS Mat_Aksesoris ")
          .append("FROM PRODUK p ")
          .append("LEFT JOIN PAKAIAN pk ON p.SKU_Produk = pk.SKU_Produk ")
          .append("LEFT JOIN SEPATU s ON p.SKU_Produk = s.SKU_Produk ")
          .append("LEFT JOIN AKSESORIS a ON p.SKU_Produk = a.SKU_Produk ")
          .append("WHERE 1=1 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append("AND (p.Nama_Produk LIKE ? OR p.Merek LIKE ? OR p.Deskripsi LIKE ?) ");
        }
        if (idKategoriFilter != null) {
            sb.append("AND p.ID_Kategori = ? ");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {

            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String wildCard = "%" + keyword + "%";
                ps.setString(paramIndex++, wildCard);
                ps.setString(paramIndex++, wildCard);
                ps.setString(paramIndex++, wildCard);
            }
            if (idKategoriFilter != null) {
                ps.setInt(paramIndex++, idKategoriFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sku = rs.getString("SKU_Produk");
                    String nama = rs.getString("Nama_Produk");
                    String desc = rs.getString("Deskripsi");
                    String merek = rs.getString("Merek");
                    double harga = rs.getDouble("Harga_Jual");
                    int katId = rs.getInt("ID_Kategori");

                                        if (rs.getString("Jenis_Potongan") != null) {
                        Pakaian pak = new Pakaian(sku, nama, desc, merek, harga, katId,
                                rs.getString("Jenis_Potongan"), rs.getString("Panduan_Perawatan"));
                        list.add(pak);
                    } else if (rs.getString("Jenis_Sepatu") != null) {
                        Sepatu sep = new Sepatu(sku, nama, desc, merek, harga, katId,
                                rs.getString("Mat_Sepatu"), rs.getString("Jenis_Sepatu"));
                        list.add(sep);
                    } else if (rs.getString("Jenis_Aksesoris") != null) {
                        Aksesoris aks = new Aksesoris(sku, nama, desc, merek, harga, katId,
                                rs.getString("Jenis_Aksesoris"), rs.getString("Mat_Aksesoris"));
                        list.add(aks);
                    } else {
                                                Produk prod = new Produk();
                        prod.setSkuProduk(sku);
                        prod.setNamaProduk(nama);
                        prod.setDeskripsi(desc);
                        prod.setMerek(merek);
                        prod.setHargaJual(harga);
                        prod.setIdKategori(katId);
                        list.add(prod);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


        public boolean insertProdukLengkap(Produk prod, String subType, Object detailSub) {
        String sqlProduk = "INSERT INTO PRODUK (SKU_Produk, Nama_Produk, Deskripsi, Merek, Harga_Jual, ID_Kategori) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

                        try (PreparedStatement psProd = conn.prepareStatement(sqlProduk)) {
                psProd.setString(1, prod.getSkuProduk());
                psProd.setString(2, prod.getNamaProduk());
                psProd.setString(3, prod.getDeskripsi());
                psProd.setString(4, prod.getMerek());
                psProd.setDouble(5, prod.getHargaJual());
                psProd.setInt(6, prod.getIdKategori());
                psProd.executeUpdate();
            }

                        if (subType.equalsIgnoreCase("PAKAIAN") && detailSub instanceof Pakaian) {
                Pakaian pak = (Pakaian) detailSub;
                String sqlPak = "INSERT INTO PAKAIAN (SKU_Produk, Jenis_Potongan, Panduan_Perawatan) VALUES (?, ?, ?)";
                try (PreparedStatement psPak = conn.prepareStatement(sqlPak)) {
                    psPak.setString(1, prod.getSkuProduk());
                    psPak.setString(2, pak.getJenisPotongan());
                    psPak.setString(3, pak.getPanduanPerawatan());
                    psPak.executeUpdate();
                }
            } else if (subType.equalsIgnoreCase("SEPATU") && detailSub instanceof Sepatu) {
                Sepatu sep = (Sepatu) detailSub;
                String sqlSep = "INSERT INTO SEPATU (SKU_Produk, Material_Pembuat, Jenis_Sepatu) VALUES (?, ?, ?)";
                try (PreparedStatement psSep = conn.prepareStatement(sqlSep)) {
                    psSep.setString(1, prod.getSkuProduk());
                    psSep.setString(2, sep.getMaterialPembuat());
                    psSep.setString(3, sep.getJenisSepatu());
                    psSep.executeUpdate();
                }
            } else if (subType.equalsIgnoreCase("AKSESORIS") && detailSub instanceof Aksesoris) {
                Aksesoris aks = (Aksesoris) detailSub;
                String sqlAks = "INSERT INTO AKSESORIS (SKU_Produk, Jenis_Aksesoris, Material_Pembuat) VALUES (?, ?, ?)";
                try (PreparedStatement psAks = conn.prepareStatement(sqlAks)) {
                    psAks.setString(1, prod.getSkuProduk());
                    psAks.setString(2, aks.getJenisAksesoris());
                    psAks.setString(3, aks.getMaterialPembuat());
                    psAks.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

        public boolean deleteProduk(String skuProduk) {
        String sql = "DELETE FROM PRODUK WHERE SKU_Produk = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, skuProduk);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


        public boolean insertVarian(VarianProduk varian) {
        String sql = "INSERT INTO VARIAN_PRODUK (ID_Varian, Warna, Ukuran, Stok, SKU_Produk) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, varian.getIdVarian());
            ps.setString(2, varian.getWarna());
            ps.setString(3, varian.getUkuran());
            ps.setInt(4, varian.getStok());
            ps.setString(5, varian.getSkuProduk());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        public boolean updateStokVarian(int idVarian, int stokBaru) {
        String sql = "UPDATE VARIAN_PRODUK SET Stok = ? WHERE ID_Varian = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stokBaru);
            ps.setInt(2, idVarian);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        public List<VarianProduk> getVarianByProduk(String skuProduk) {
        List<VarianProduk> list = new ArrayList<>();
        String sql = "SELECT * FROM VARIAN_PRODUK WHERE SKU_Produk = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, skuProduk);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VarianProduk vp = new VarianProduk(
                        rs.getInt("ID_Varian"),
                        rs.getString("Warna"),
                        rs.getString("Ukuran"),
                        rs.getInt("Stok"),
                        rs.getString("Sku_Produk")
                    );
                    list.add(vp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
} 