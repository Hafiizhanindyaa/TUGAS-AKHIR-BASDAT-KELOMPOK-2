# TUGAS AKHIR BASIS KELOMPOK 2
## Anggota Kelompok
|No|Nama|NIM|
|---|---|---|
|1|Muhammad Hafizh Raihan|255150200111036|
|2|Naufal Fadhil Arkani|255150200111037|
|3|Hafiz Adli Assyahadat|255150200111038|
|4|Muhammad Hafiizh Anindya|255150207111057|

## Deskripsi
 
**Zalora E-Commerce System** adalah aplikasi manajemen penjualan berbasis *desktop* yang dikembangkan menggunakan Java dengan antarmuka grafis Java Swing. Sistem ini mensimulasikan alur belanja *online* secara lengkap, mulai dari penelusuran katalog produk, manajemen keranjang belanja, proses *checkout* dengan voucher diskon, hingga pengelolaan pesanan dan riwayat transaksi.
 
Aplikasi ini mengakomodasi dua peran pengguna dalam satu sistem terintegrasi:
- **Front-End (Pelanggan)** — berinteraksi langsung dengan katalog, keranjang, dan transaksi
- **Back-End (Administrator)** — mengelola inventori, operasional, voucher, pesanan, dan analisis data

## Fitur Utama
 
###  Sisi Pelanggan (Front-End)
| Menu | Fitur |
|------|-------|
| Katalog Produk | Pencarian via `SP_CARI_PRODUK`, filter tipe, kategori, rentang harga, sorting |
| Keranjang Belanja | Tambah, hapus item, lihat subtotal |
| Checkout & Pembayaran | Pilih alamat, kurir, metode; validasi & apply voucher; bayar via Saldo Zalora |
| Saldo & Top Up | Top up saldo, riwayat mutasi (TOPUP / PEMBAYARAN / REFUND) |
| Profil & Alamat | Edit akun, CRUD alamat pengiriman, CRUD nomor telepon |
| Riwayat Pesanan | Filter tahun/bulan/status via `VW_RIWAYAT_PESANAN` |
| Ulasan Produk | Beri, edit, hapus rating bintang 1–5 (hanya untuk pesanan selesai) |
 
###  Sisi Administrator (Back-End)
| Menu | Fitur |
|------|-------|
| Kelola Inventory | CRUD produk + subtype, manajemen varian & stok |
| Kelola Operasional | CRUD kurir, kategori, metode pembayaran |
| Kelola Voucher Promo | CRUD voucher diskon (persentase / nominal) |
| Pantau Pesanan | Filter & update status pesanan, auto-refund saldo saat dibatalkan |
| Top 5 Produk Terlaris | Analisis via `VW_TOP5_PRODUK_TERLARIS` |
| Top 5 Pelanggan | Analisis via `SP_TOP5_PELANGGAN_TERBANYAK_BELANJA` dengan filter periode |
| Produk Sering Dibeli Bersamaan | Analisis asosiasi via self-join & correlated subquery |
 
---
