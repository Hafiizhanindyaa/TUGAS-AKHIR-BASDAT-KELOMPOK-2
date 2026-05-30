package main;

import dao.TransactionDAO;

public class TestAnalysisQuery {
    public static void main(String[] args) {
        System.out.println("=== MEMULAI PENGUJIAN QUERY ANALISIS BASIS DATA ===");
        TransactionDAO txDAO = new TransactionDAO();

        txDAO.cetakProdukTerlaris();
        System.out.println();

        double omset = txDAO.getTotalPendapatanSelesai();
        System.out.println("Total Pendapatan Toko (Status Selesai): Rp " + omset);
        System.out.println();

        txDAO.cetakStatistikPenggunaanVoucher();
        System.out.println("\n=== PENGUJIAN DATA BERHASIL DISELESAIKAN ===");
    }
}