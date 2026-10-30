package com.campusfind;

import com.campusfind.model.Admin;
import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.CleaningService;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.Klaim;
import com.campusfind.model.Laporan;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Security;
import com.campusfind.model.StatusLaporan;
import com.campusfind.model.User;
import com.campusfind.service.SistemLostAndFound;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * CampusFind - Sistem Smart Lost and Found Berbasis Java.
 * Aplikasi konsol dengan menu sesuai peran: Admin, Mahasiswa, Petugas (CS & Security).
 */
public class Main {
    private static final SistemLostAndFound sistem = new SistemLostAndFound();
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("   CAMPUSFIND v2.0 - Smart Lost and Found     ");
        System.out.println("   Auto-Matching | QR Code | Verifikasi Akun  ");
        System.out.println("==============================================");
        if (args.length > 0 && args[0].equalsIgnoreCase("--demo")) {
            jalankanDemoSkenario();
            return;
        }
        boolean jalan = true;
        while (jalan) {
            User u = sistem.getSesiLogin();
            if (u == null) {
                jalan = menuUmum();
            } else if (u instanceof Mahasiswa m) {
                jalan = menuMahasiswa(m);
            } else if (u instanceof CleaningService cs) {
                jalan = menuCleaningService(cs);
            } else if (u instanceof Security sec) {
                jalan = menuSecurity(sec);
            } else if (u instanceof Admin a) {
                jalan = menuAdmin(a);
            }
        }
        System.out.println("Terima kasih telah menggunakan CampusFind. Sampai jumpa!");
    }

    // ==================== Demo Otomatis (skenario BAB IV) ====================

    private static void jalankanDemoSkenario() {
        List<User> u = sistem.getDaftarUser();
        CleaningService cs = (CleaningService) u.stream().filter(x -> x instanceof CleaningService)
                .findFirst().orElseThrow();
        Security sec = (Security) u.stream().filter(x -> x instanceof Security)
                .findFirst().orElseThrow();
        Mahasiswa fakhry = (Mahasiswa) u.stream()
                .filter(x -> x instanceof Mahasiswa m && m.getNim().equals("251402053"))
                .findFirst().orElseThrow();
        Mahasiswa nadia = (Mahasiswa) u.stream()
                .filter(x -> x instanceof Mahasiswa m && m.getNim().equals("251402073"))
                .findFirst().orElseThrow();
        Admin admin = (Admin) u.stream().filter(x -> x instanceof Admin)
                .findFirst().orElseThrow();

        System.out.println("\n>>> SKENARIO 1: Dompet ditemukan CS, dicocokkan otomatis, diklaim & disetujui Security");
        Laporan temuanDompet = sistem.cariLaporan(2);
        System.out.println(cs.serahkanBarangKePos(temuanDompet, sec));
        sistem.catatSemua("Barang #" + temuanDompet.getId() + " diserahkan CS ke pos Security");
        System.out.println(sec.cetakQR(temuanDompet));

        System.out.println(sistem.ajukanKlaim(fakhry, temuanDompet,
                "Ada stiker biru, berisi kartu mahasiswa dan uang 45 ribu."));
        Klaim k1 = fakhry.getKlaimSaya().get(fakhry.getKlaimSaya().size() - 1);
        boolean cocokIsi = ((com.campusfind.model.BarangNonElektronik) temuanDompet.getItem())
                .cocokDenganIsi(k1.getDeskripsiBukti());
        System.out.println("Uji bukti oleh Security: " + (cocokIsi ? "COCOK" : "TIDAK COCOK"));
        System.out.println("=== VERIFIKASI PETUGAS (SECURITY) ===");
        System.out.println(sistem.prosesVerifikasi(sec, k1, cocokIsi));
        System.out.println("Serah terima tercatat pada audit log.");

        System.out.println("\n>>> SKENARIO 2: Laptop elektronik - verifikasi OTP berbasis akun terdaftar");
        // simulasikan temuan laptop oleh security
        BarangElektronik laptopTemuan = new BarangElektronik(sistem.nextItemId(), "Laptop", "Elektronik", "Hitam",
                "Goresan di sudut kanan", "Asus", "VivoBook 14", "nadia@student.ac.id", "");
        Laporan ltLaptop = sistem.buatLaporan(sec, laptopTemuan, JenisLaporan.TEMUAN,
                "Perpustakaan", LocalDateTime.now());
        System.out.println(sec.cetakQR(ltLaptop));
        System.out.println(sistem.ajukanKlaim(nadia, ltLaptop, "Saya pemilik akun Google di laptop."));
        Klaim k2 = nadia.getKlaimSaya().get(nadia.getKlaimSaya().size() - 1);
        String otp = k2.kirimOTP();
        System.out.println("=== CONTOH VERIFIKASI BARANG ELEKTRONIK ===");
        System.out.println("OTP dikirim ke akun terdaftar perangkat: "
                + BarangElektronik.samarkan(laptopTemuan.getAkunTerdaftar())
                + "  [SIMULASI kode: " + otp + "]");
        System.out.println("OTP dimasukkan pengklaim: " + (k2.cekOTP(otp) ? "COCOK" : "GAGAL")
                + ". Lanjut verifikasi fisik oleh Security.");
        System.out.println(sistem.prosesVerifikasi(sec, k2, true));

        System.out.println("\n>>> Anti klaim ganda: klaim kedua pada barang yang sama harus ditolak");
        System.out.println(sistem.ajukanKlaim(nadia, temuanDompet, "Dompet itu punya saya juga!"));

        System.out.println("\n" + fakhry.tampilDashboard());
        System.out.println(admin.lihatStatistik(sistem.getDaftarLaporan(), sistem.getDaftarKlaim()));
        System.out.println("=== AUDIT LOG ===");
        admin.lihatAuditLog();
    }

    // ==================== Menu Umum / Login ====================

    private static boolean menuUmum() {
        System.out.println("\n----- MENU UTAMA -----");
        System.out.println("1. Login");
        System.out.println("2. Lihat pengumuman barang temuan (publik, tanpa nama pelapor)");
        System.out.println("3. Akun demo");
        System.out.println("0. Keluar");
        String p = input("Pilih: ");
        switch (p) {
            case "1" -> {
                System.out.print("Email : ");
                String email = in.nextLine().trim();
                System.out.print("Password : ");
                String pass = in.nextLine().trim();
                if (sistem.login(email, pass)) {
                    System.out.println("Login berhasil sebagai " + sistem.getSesiLogin().getNama()
                            + " (" + sistem.getSesiLogin().getPeran() + ").");
                } else {
                    System.out.println("Login gagal: email/password salah.");
                }
            }
            case "2" -> tampilPublik();
            case "3" -> {
                System.out.println("""
                        === AKUN DEMO ===
                        Admin          : admin@campus.ac.id / admin123
                        Security       : bambang.sec@campus.ac.id / sec123
                        CleaningService: joko.cs@campus.ac.id / cs123
                        Mahasiswa      : fakhry@student.ac.id / mhs123 (NIM 251402053)
                        Mahasiswa      : nadia@student.ac.id / mhs123 (NIM 251402073)
                        """);
            }
            case "0" -> {
                return false;
            }
            default -> System.out.println("Pilihan tidak dikenal.");
        }
        return true;
    }

    private static void tampilPublik() {
        List<Laporan> temuan = sistem.getLaporanByJenis(JenisLaporan.TEMUAN);
        System.out.println("\n=== BARANG TEMUAN (Publik) ===");
        if (temuan.isEmpty()) {
            System.out.println("(Belum ada barang temuan)");
            return;
        }
        for (Laporan l : temuan) {
            if (l.getStatus() == StatusLaporan.DIARSIPKAN) continue;
            System.out.println("#" + l.getId() + " - " + l.tampilkanKePublik()
                    + " [Status: " + l.getStatus().getLabel() + "]");
        }
        System.out.println("Catatan: identitas pelapor disembunyikan untuk menjaga privasi.");
    }

    // ==================== Menu Mahasiswa ====================

    private static boolean menuMahasiswa(Mahasiswa m) {
        System.out.println("\n===== MAHASISWA: " + m.getNama() + " =====");
        System.out.println("1. Dashboard saya");
        System.out.println("2. Buat laporan BARANG HILANG");
        System.out.println("3. Lihat rekomendasi kecocokan (Auto-Matching)");
        System.out.println("4. Ajukan klaim atas barang temuan");
        System.out.println("5. Cek status klaim saya");
        System.out.println("0. Logout");
        String p = input("Pilih: ");
        switch (p) {
            case "1" -> System.out.println(m.tampilDashboard());
            case "2" -> buatLaporanHilang(m);
            case "3" -> lihatRekomendasi(m);
            case "4" -> ajukanKlaim(m);
            case "5" -> {
                if (m.getKlaimSaya().isEmpty()) System.out.println("(Belum ada klaim)");
                for (Klaim k : m.getKlaimSaya()) {
                    System.out.printf("Klaim #%d -> Laporan Temuan #%d : %s%n",
                            k.getId(), k.getLaporanTemuan().getId(), k.getStatus().getLabel());
                }
            }
            case "0" -> sistem.logout();
            default -> System.out.println("Pilihan tidak dikenal.");
        }
        return true;
    }

    private static void buatLaporanHilang(Mahasiswa m) {
        Item item = tanyaDetailBarang();
        String lokasi = tanyaLokasi();
        Laporan l = sistem.buatLaporan(m, item, JenisLaporan.HILANG, lokasi, LocalDateTime.now());
        m.buatLaporan(l);
        sistem.catatSemua("Laporan HILANG #" + l.getId() + " dibuat " + m.getNama());
        System.out.println("Laporan hilang #" + l.getId() + " tersimpan. "
                + "Sistem akan otomatis mencocokkan dengan data temuan.");
    }

    private static void lihatRekomendasi(Mahasiswa m) {
        List<Laporan> hilang = m.getLaporanSaya().stream()
                .filter(l -> l.getJenis() == JenisLaporan.HILANG).toList();
        if (hilang.isEmpty()) {
            System.out.println("(Anda belum memiliki laporan barang hilang)");
            return;
        }
        for (Laporan h : hilang) {
            System.out.println("\nCocokkan laporan #" + h.getId() + " (" + h.getItem().getNama() + "):");
            List<Laporan> rec = sistem.getRekomendasi(h);
            if (rec.isEmpty()) {
                System.out.println("  Belum ada temuan yang cocok (ambang skor >= "
                        + com.campusfind.service.MatchingEngine.AMBANG_REKOMENDASI + ").");
            }
            for (Laporan t : rec) {
                int skor = sistem.getMatchingEngine().hitungSkor(h, t);
                System.out.println("  Kandidat #" + t.getId() + ": " + t.tampilkanKePublik()
                        + " | Skor: " + skor + "/100 | QR: "
                        + (t.getKodeQR() == null ? "-" : t.getKodeQR()));
            }
        }
    }

    private static void ajukanKlaim(Mahasiswa m) {
        List<Laporan> temuan = sistem.getLaporanByJenis(JenisLaporan.TEMUAN).stream()
                .filter(l -> l.getStatus() != StatusLaporan.DIKEMBALIKAN
                        && l.getStatus() != StatusLaporan.DIARSIPKAN)
                .toList();
        if (temuan.isEmpty()) {
            System.out.println("(Tidak ada barang temuan yang bisa diklaim)");
            return;
        }
        System.out.println("Pilih laporan temuan:");
        for (Laporan t : temuan) {
            System.out.println("  #" + t.getId() + " - " + t.tampilkanKePublik()
                    + " [" + t.getStatus().getLabel() + "]");
        }
        int id = Integer.parseInt(input("ID temuan: "));
        Laporan t = sistem.cariLaporan(id);
        if (t == null || t.getJenis() != JenisLaporan.TEMUAN) {
            System.out.println("Laporan tidak ditemukan.");
            return;
        }
        String bukti = input("Deskripsikan bukti kepemilikan (isi/ciri khusus/akun terdaftar): ");
        System.out.println(sistem.ajukanKlaim(m, t, bukti));
        if (t.getItem() instanceof BarangElektronik be) {
            System.out.println("=== VERIFIKASI BARANG ELEKTRONIK (Challenge-Response) ===");
            Klaim k = m.getKlaimSaya().get(m.getKlaimSaya().size() - 1);
            String otp = k.kirimOTP();
            System.out.println("OTP dikirim ke akun terdaftar perangkat: "
                    + BarangElektronik.samarkan(be.getAkunTerdaftar())
                    + "  [SIMULASI kode: " + otp + "]");
            String masuk = input("Masukkan OTP dari perangkat Anda: ");
            if (k.cekOTP(masuk)) {
                System.out.println("OTP COCOK. Lanjut verifikasi fisik oleh Security di pos.");
            } else {
                System.out.println("OTP TIDAK COCOK. Jalur recovery akun tersedia bila lupa.");
            }
        } else {
            System.out.println("Bukti non-elektronik dicatat. Security akan menguji "
                    + "kecocokan isi barang saat verifikasi.");
        }
    }

    // ==================== Menu Cleaning Service ====================

    private static boolean menuCleaningService(CleaningService cs) {
        System.out.println("\n===== CLEANING SERVICE: " + cs.getNama() + " =====");
        System.out.println("1. Buat laporan BARANG TEMUAN");
        System.out.println("2. Serahkan barang ke pos Security");
        System.out.println("3. Daftar laporan saya");
        System.out.println("0. Logout");
        String p = input("Pilih: ");
        switch (p) {
            case "1" -> {
                Item item = tanyaDetailBarang();
                String lokasi = tanyaLokasi();
                Laporan l = sistem.buatLaporan(cs, item, JenisLaporan.TEMUAN, lokasi, LocalDateTime.now());
                cs.buatLaporan(l);
                sistem.catatSemua("Laporan TEMUAN #" + l.getId() + " dibuat CS " + cs.getNama());
                System.out.println("Laporan temuan #" + l.getId() + " tersimpan. "
                        + "Sistem auto-matching berjalan terhadap laporan hilang.");
            }
            case "2" -> {
                if (cs.getLaporanSaya().isEmpty()) {
                    System.out.println("(Belum ada temuan dari Anda)");
                    break;
                }
                for (Laporan l : cs.getLaporanSaya()) {
                    System.out.println("  #" + l.getId() + " - " + l.getItem().getNama()
                            + " [" + l.getStatus().getLabel() + "]");
                }
                int id = Integer.parseInt(input("ID temuan yang diserahkan: "));
                Laporan l = sistem.cariLaporan(id);
                Security penerima = (Security) sistem.getDaftarUser().stream()
                        .filter(u -> u instanceof Security).findFirst().orElse(null);
                if (l != null && penerima != null) {
                    System.out.println(cs.serahkanBarangKePos(l, penerima));
                    sistem.catatSemua("Barang #" + l.getId() + " diserahkan CS ke pos Security");
                }
            }
            case "3" -> {
                for (Laporan l : cs.getLaporanSaya()) {
                    System.out.println("#" + l.getId() + " - " + l.tampilkanKePublik()
                            + " [" + l.getStatus().getLabel() + "]");
                }
            }
            case "0" -> sistem.logout();
            default -> System.out.println("Pilihan tidak dikenal.");
        }
        return true;
    }

    // ==================== Menu Security ====================

    private static boolean menuSecurity(Security sec) {
        System.out.println("\n===== SECURITY: " + sec.getNama() + " =====");
        System.out.println("1. Daftar klaim menunggu verifikasi");
        System.out.println("2. Verifikasi klaim (putuskan setuju/tolak)");
        System.out.println("3. Cetak QR Code laporan temuan");
        System.out.println("4. Lihat detail lengkap laporan (hak petugas)");
        System.out.println("0. Logout");
        String p = input("Pilih: ");
        switch (p) {
            case "1" -> {
                List<Klaim> antrian = sistem.getKlaimMenunggu();
                if (antrian.isEmpty()) System.out.println("(Tidak ada klaim menunggu)");
                for (Klaim k : antrian) {
                    System.out.println("Klaim #" + k.getId() + " oleh " + k.getPengklaim().getNama()
                            + " -> Temuan #" + k.getLaporanTemuan().getId()
                            + " [" + k.getStatus().getLabel() + "]");
                    System.out.println("  Bukti: \"" + k.getDeskripsiBukti() + "\"");
                }
            }
            case "2" -> verifikasiKlaim(sec);
            case "3" -> {
                int id = Integer.parseInt(input("ID laporan temuan: "));
                Laporan l = sistem.cariLaporan(id);
                if (l != null && l.getJenis() == JenisLaporan.TEMUAN) {
                    System.out.println(sec.cetakQR(l));
                } else {
                    System.out.println("Laporan temuan tidak ditemukan.");
                }
            }
            case "4" -> {
                int id = Integer.parseInt(input("ID laporan: "));
                Laporan l = sistem.cariLaporan(id);
                System.out.println(l == null ? "Tidak ditemukan." : l.tampilkanLengkap());
            }
            case "0" -> sistem.logout();
            default -> System.out.println("Pilihan tidak dikenal.");
        }
        return true;
    }

    private static void verifikasiKlaim(Security sec) {
        List<Klaim> antrian = sistem.getKlaimMenunggu();
        if (antrian.isEmpty()) {
            System.out.println("(Tidak ada klaim menunggu)");
            return;
        }
        for (Klaim k : antrian) {
            System.out.println("Klaim #" + k.getId() + " -> Temuan #" + k.getLaporanTemuan().getId()
                    + " | Bukti: \"" + k.getDeskripsiBukti() + "\"");
        }
        int id = Integer.parseInt(input("ID klaim: "));
        Klaim k = sistem.cariKlaim(id);
        if (k == null) {
            System.out.println("Klaim tidak ditemukan.");
            return;
        }
        Item item = k.getLaporanTemuan().getItem();
        boolean cocok;
        if (item instanceof BarangNonElektronik bne) {
            cocok = bne.cocokDenganIsi(k.getDeskripsiBukti());
            System.out.println(cocok
                    ? "Hasil uji: deskripsi pengklaim COCOK dengan isi barang sebenarnya."
                    : "Hasil uji: deskripsi pengklaim TIDAK cocok dengan isi barang.");
        } else {
            System.out.println("Barang elektronik: pastikan pengklaim telah lolos OTP/unlock perangkat.");
            String lolos = input("Apakah pengklaim lolos uji perangkat? (y/n): ");
            cocok = lolos.equalsIgnoreCase("y");
        }
        String keputusan = input(cocok
                ? "Sistem menilai bukti cocok. Setujui klaim? (y/n): "
                : "Sistem menilai bukti lemah. Tetap setujui? (y/n): ");
        boolean disetujui = keputusan.equalsIgnoreCase("y");
        System.out.println("=== VERIFIKASI PETUGAS (SECURITY) ===");
        System.out.println(sistem.prosesVerifikasi(sec, k, disetujui));
        if (disetujui) {
            System.out.println("Serah terima tercatat pada audit log.");
        }
    }

    // ==================== Menu Admin ====================

    private static boolean menuAdmin(Admin a) {
        System.out.println("\n===== ADMIN: " + a.getNama() + " =====");
        System.out.println("1. Statistik sistem");
        System.out.println("2. Audit log");
        System.out.println("3. Kelola master data (lokasi/kategori)");
        System.out.println("4. Arsipkan barang lama (>60 hari)");
        System.out.println("5. Lihat semua laporan");
        System.out.println("0. Logout");
        String p = input("Pilih: ");
        switch (p) {
            case "1" -> System.out.println(a.lihatStatistik(sistem.getDaftarLaporan(),
                    sistem.getDaftarKlaim()));
            case "2" -> a.lihatAuditLog();
            case "3" -> {
                System.out.println("Data LOKASI    : " + a.getDaftarLokasi());
                System.out.println("Data KATEGORI  : " + a.getDaftarKategori());
                String jenis = input("Jenis data (lokasi/kategori): ");
                String aksi = input("(t)ambah / (h)apus: ");
                String nilai = input("Nilai: ");
                a.kelolaMasterData(aksi.equalsIgnoreCase("t"), jenis.toUpperCase(), nilai);
                System.out.println("Master data diperbarui.");
            }
            case "4" -> {
                int n = a.arsipkanBarangLama(sistem.getDaftarLaporan(), 60);
                System.out.println(n + " laporan diarsipkan (batas 60 hari).");
            }
            case "5" -> {
                for (Laporan l : sistem.getDaftarLaporan()) {
                    System.out.println(l.tampilkanLengkap());
                }
            }
            case "0" -> sistem.logout();
            default -> System.out.println("Pilihan tidak dikenal.");
        }
        return true;
    }

    // ==================== Util Input ====================

    private static Item tanyaDetailBarang() {
        String nama = input("Nama barang (cth: Dompet/Laptop): ");
        String kategori = input("Kategori (Elektronik/Aksesoris Pribadi/Dokumen/Tas): ");
        String warna = input("Warna: ");
        String ciri = input("Ciri khusus: ");
        boolean elektronik = kategori.equalsIgnoreCase("Elektronik");
        if (elektronik) {
            String merk = input("Merk: ");
            String tipe = input("Tipe: ");
            String akun = input("Akun/no HP terdaftar di perangkat: ");
            String seri = input("Nomor seri (opsional, enter jika tidak tahu): ");
            return new BarangElektronik(sistem.nextItemId(), nama, kategori, warna, ciri,
                    merk, tipe, akun, seri);
        }
        String isi = input("Isi/ciri detail privat (untuk uji klaim, cth: kartu mahasiswa, uang 45 ribu): ");
        return new BarangNonElektronik(sistem.nextItemId(), nama, kategori, warna, ciri, isi);
    }

    private static String tanyaLokasi() {
        List<String> lokasi = ((Admin) sistem.getDaftarUser().stream()
                .filter(u -> u instanceof Admin).findFirst().orElseThrow()).getDaftarLokasi();
        System.out.println("Pilihan lokasi master data: " + lokasi);
        return input("Lokasi kejadian: ");
    }

    private static String input(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) { // stdin habis (misal input dari file pipe)
            System.out.println();
            System.out.println("(Input habis - aplikasi dihentikan.)");
            System.exit(0);
        }
        return in.nextLine().trim();
    }
}
