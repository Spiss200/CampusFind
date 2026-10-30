package com.campusfind.service;

import com.campusfind.model.Admin;
import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.CleaningService;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.Klaim;
import com.campusfind.model.Laporan;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Notifikasi;
import com.campusfind.model.Security;
import com.campusfind.model.StatusKlaim;
import com.campusfind.model.StatusLaporan;
import com.campusfind.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SistemLostAndFound: pusat penyimpanan data in-memory (komposisi objek)
 * dan orkestrasi alur bisnis. Integrasi database = tahap lanjutan.
 */
public class SistemLostAndFound {
    private final List<User> daftarUser = new ArrayList<>();
    private final List<Laporan> daftarLaporan = new ArrayList<>();
    private final List<Klaim> daftarKlaim = new ArrayList<>();
    private final MatchingEngine matchingEngine = new MatchingEngine();
    private User sesiLogin;

    private int nextLaporanId = 1;
    private int nextKlaimId = 1;
    private int nextItemId = 1;

    public SistemLostAndFound() {
        seedData();
    }

    /** Data awal simulasi sesuai skenario dokumen (BAB IV). */
    private void seedData() {
        Admin admin = new Admin(1, "Rina Wijaya", "admin@campus.ac.id", "admin123");
        CleaningService cs = new CleaningService(2, "Joko Susilo", "joko.cs@campus.ac.id", "cs123");
        Security sec = new Security(3, "Bambang Prasetyo", "bambang.sec@campus.ac.id", "sec123");
        Mahasiswa mhs = new Mahasiswa(4, "Fakhry Adrian", "fakhry@student.ac.id", "mhs123", "251402053");
        Mahasiswa mhs2 = new Mahasiswa(5, "Nadia Situmorang", "nadia@student.ac.id", "mhs123", "251402073");
        daftarUser.addAll(List.of(admin, cs, sec, mhs, mhs2));

        // Barang & laporan hilang milik Fakhry: dompet hitam di Ruang Kelas Gedung A
        Item dompetHilang = new BarangNonElektronik(nextItemId++, "Dompet", "Aksesoris Pribadi",
                "Hitam", "Ada stiker biru",
                "kartu mahasiswa, uang 45 ribu, ktp");
        Laporan lh = buatLaporan(mhs, dompetHilang, JenisLaporan.HILANG,
                "Ruang Kelas Gedung A", LocalDateTime.now().minusDays(1));
        mhs.buatLaporan(lh);

        // CS menemukan dompet serupa -> laporan TEMUAN
        Item dompetTemuan = new BarangNonElektronik(nextItemId++, "Dompet", "Aksesoris Pribadi",
                "Hitam", "Ada stiker biru",
                "kartu mahasiswa, uang 45 ribu, ktp");
        Laporan lt = buatLaporan(cs, dompetTemuan, JenisLaporan.TEMUAN,
                "Ruang Kelas Gedung A", LocalDateTime.now());
        cs.buatLaporan(lt);

        // Tambahan: laptop hilang milik Nadia (elektronik, utk demo OTP)
        Item laptop = new BarangElektronik(nextItemId++, "Laptop", "Elektronik", "Hitam",
                "Goresan di sudut kanan", "Asus", "VivoBook 14",
                "nadia@student.ac.id", "ASM-99120-XZ");
        Laporan lhLaptop = buatLaporan(mhs2, laptop, JenisLaporan.HILANG,
                "Perpustakaan", LocalDateTime.now().minusDays(2));
        mhs2.buatLaporan(lhLaptop);
    }

    // ================= Autentikasi =================

    public boolean login(String email, String password) {
        for (User u : daftarUser) {
            if (u.login(email, password)) {
                sesiLogin = u;
                return true;
            }
        }
        return false;
    }

    public void logout() {
        if (sesiLogin != null) sesiLogin.logout();
        sesiLogin = null;
    }

    public User getSesiLogin() {
        return sesiLogin;
    }

    // ================= Laporan =================

    public Laporan buatLaporan(User pelapor, Item item, JenisLaporan jenis,
                               String lokasi, LocalDateTime waktu) {
        Laporan l = new Laporan(nextLaporanId++, pelapor, item, jenis, lokasi, waktu);
        daftarLaporan.add(l);
        matchingEngine.jalankanAutoMatching(daftarLaporan);
        return l;
    }

    public List<Laporan> getDaftarLaporan() {
        return daftarLaporan;
    }

    public List<Laporan> getLaporanByJenis(JenisLaporan jenis) {
        return daftarLaporan.stream().filter(l -> l.getJenis() == jenis).toList();
    }

    public Laporan cariLaporan(int id) {
        return daftarLaporan.stream().filter(l -> l.getId() == id).findFirst().orElse(null);
    }

    public List<Laporan> getRekomendasi(Laporan hilang) {
        return matchingEngine.cariRekomendasi(hilang, daftarLaporan);
    }

    public MatchingEngine getMatchingEngine() {
        return matchingEngine;
    }

    // ================= Klaim =================

    /** Kembalikan pesan hasil pengajuan klaim. */
    public String ajukanKlaim(Mahasiswa mhs, Laporan temuan, String bukti) {
        Klaim k = new Klaim(nextKlaimId++, mhs, temuan, bukti);
        if (!k.ajukan()) {
            return "Klaim DITOLAK sistem: barang sedang diproses klaim lain / sudah selesai.";
        }
        daftarKlaim.add(k);
        mhs.ajukanKlaim(k);
        catatSemua("Klaim #" + k.getId() + " diajukan " + mhs.getNama()
                + " untuk laporan #" + temuan.getId());
        StringBuilder sb = new StringBuilder();
        sb.append("Klaim diajukan. Status laporan: ")
          .append(temuan.getStatus().getLabel()).append("\n");
        sb.append("Bukti yang diminta (").append(temuan.getItem() instanceof BarangElektronik ? "elektronik" : "non-elektronik")
          .append("):\n");
        for (var jb : temuan.getItem().getSyaratBuktiUtama()) {
            sb.append("  - ").append(jb.getDeskripsi()).append("\n");
        }
        return sb.toString();
    }

    public List<Klaim> getKlaimMenunggu() {
        return daftarKlaim.stream()
                .filter(k -> k.getStatus() == StatusKlaim.MENUNGGU
                        || k.getStatus() == StatusKlaim.DIVERIFIKASI)
                .toList();
    }

    public Klaim cariKlaim(int id) {
        return daftarKlaim.stream().filter(k -> k.getId() == id).findFirst().orElse(null);
    }

    /** Verifikasi akhir oleh Security (satu-satunya Verifikator). */
    public String prosesVerifikasi(Security sec, Klaim k, boolean disetujui) {
        sec.verifikasiKlaim(k, disetujui);
        catatSemua("Klaim #" + k.getId() + " " + (disetujui ? "DISETUJUI" : "DITOLAK")
                + " oleh Security " + sec.getNama());
        Notifikasi n = new Notifikasi(k.getPengklaim(), disetujui
                ? "Klaim Anda DISETUJUI. Silakan ambil barang di pos Security. Status: DIKEMBALIKAN."
                : "Klaim Anda DITOLAK karena bukti tidak memadai.");
        n.kirimNotif();
        return "Klaim #" + k.getId() + " -> " + k.getStatus().getLabel()
                + " | Laporan #" + k.getLaporanTemuan().getId() + " -> "
                + k.getLaporanTemuan().getStatus().getLabel();
    }

    // ================= Util =================

    public void catatSemua(String aktivitas) {
        for (User u : daftarUser) {
            if (u instanceof Admin a) {
                a.catatAudit(aktivitas);
                break;
            }
        }
    }

    public List<User> getDaftarUser() {
        return daftarUser;
    }

    public int nextItemId() {
        return nextItemId++;
    }

    public List<Klaim> getDaftarKlaim() {
        return daftarKlaim;
    }

    public static String statusLabel(StatusLaporan s) {
        return s.getLabel();
    }
}
