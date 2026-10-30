# CampusFind v2.0 — Sistem Smart Lost and Found Berbasis Java

Aplikasi konsol pencarian barang hilang/temuan di lingkungan kampus dengan fitur:
**Auto-Matching**, **QR Code (kode unik tracking)**, dan **Verifikasi Kepemilikan Berbasis Akun (OTP)**.

Konsep OOP yang digunakan: inheritance, encapsulation, polymorphism, abstraction,
interface (`Pelapor`, `Verifikator`), enum status, dan komposisi objek.

## Aktor & Hak Akses
| Peran | Hak |
|---|---|
| Mahasiswa | Buat laporan HILANG, lihat rekomendasi, ajukan klaim + bukti (OTP/ciri fisik) |
| Cleaning Service | Buat laporan TEMUAN, serahkan barang ke pos (TANPA hak verifikasi) |
| Security | Verifikasi klaim (satu-satunya `Verifikator`), ubah status, cetak QR, catat serah terima |
| Admin | Master data (lokasi/kategori), audit log, statistik, arsip barang lama (>60 hari) |

## Struktur Proyek
```
src/com/campusfind/
  Main.java                      # UI konsol + mode demo skenario BAB IV
  model/                         # User, Mahasiswa, Petugas, Security, CleaningService, Admin,
                                 # Item, BarangElektronik, BarangNonElektronik,
                                 # Laporan, Klaim, Notifikasi, interface & enum
  service/
    MatchingEngine.java          # pra-filter (kategori sama, <=7 hari) + skoring 0-100 (ambang 70)
    SistemLostAndFound.java      # penyimpanan in-memory + orkestrasi alur bisnis
```

## Cara Kompilasi & Menjalankan (Java 17+)
```bash
cd campusfind
javac -encoding UTF-8 -d out $(find src -name "*.java")

# Mode interaktif
java -cp out com.campusfind.Main

# Mode demo otomatis (mengulang skenario dompet & laptop pada dokumen BAB IV)
java -cp out com.campusfind.Main --demo
```

## Akun Demo
| Peran | Email | Password |
|---|---|---|
| Admin | admin@campus.ac.id | admin123 |
| Security | bambang.sec@campus.ac.id | sec123 |
| Cleaning Service | joko.cs@campus.ac.id | cs123 |
| Mahasiswa (NIM 251402053) | fakhry@student.ac.id | mhs123 |
| Mahasiswa (NIM 251402073) | nadia@student.ac.id | mhs123 |

## Alur Utama (sesuai dokumen perancangan)
1. CS menemukan barang → buat laporan TEMUAN → serahkan ke pos → Security mencetak QR tracking.
2. `MatchingEngine` berjalan otomatis setiap laporan baru masuk:
   pra-filter kategori + selisih waktu ≤ 7 hari, lalu skor (kategori 30, warna 20, lokasi 30, ciri 20).
3. Skor ≥ 70 → notifikasi ke mahasiswa pelapor; identitas pelapor disembunyikan di tampilan publik.
4. Mahasiswa mengajukan klaim → laporan terkunci `DALAM_PROSES_KLAIM` (mencegah klaim ganda).
5. Bukti: elektronik = OTP/unlock/recovery akun terdaftar; non-elektronik = uji isi & ciri fisik.
6. Security memutuskan → `DIKEMBALIKAN`/`DITOLAK`, semuanya tercatat di audit log Admin.

## Catatan
- OTP disimulasikan in-app (belum terhubung SMS/email).
- QR direpresentasikan sebagai kode unik teks (bisa dikonversi gambar via ZXing pada tahap lanjut).
- Penyimpanan masih memori; integrasi MySQL merupakan pengembangan lanjutan.
