package com.campusfind.service;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.Laporan;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Notifikasi;
import com.campusfind.model.StatusLaporan;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * MatchingEngine: class layanan pencocokan laporan HILANG vs TEMUAN.
 * Logika skor dipisahkan dari Item (single responsibility).
 */
public class MatchingEngine {

    public static final int AMBANG_REKOMENDASI = 70;
    public static final int MAX_SELISIH_HARI = 7;

    /** Pra-filter: hanya bandingkan kategori sama & selisih waktu <= 7 hari. */
    private boolean praFilter(Laporan hilang, Laporan temuan) {
        if (hilang.getJenis() != JenisLaporan.HILANG || temuan.getJenis() != JenisLaporan.TEMUAN) {
            return false;
        }
        boolean kategoriSama = hilang.getItem().getKategori()
                .equalsIgnoreCase(temuan.getItem().getKategori());
        long selisih = Math.abs(ChronoUnit.DAYS.between(hilang.getWaktu(), temuan.getWaktu()));
        boolean waktuDekat = selisih <= MAX_SELISIH_HARI;
        return kategoriSama && waktuDekat;
    }

    /** Hitung skor kecocokan 0-100. */
    public int hitungSkor(Laporan hilang, Laporan temuan) {
        if (!praFilter(hilang, temuan)) return 0;
        int skor = 0;
        Item a = hilang.getItem();
        Item b = temuan.getItem();
        if (a.getKategori().equalsIgnoreCase(b.getKategori())) skor += 30;
        if (a.getWarna().equalsIgnoreCase(b.getWarna())) skor += 20;
        if (hilang.getLokasi().equalsIgnoreCase(temuan.getLokasi())) skor += 30;
        if (!a.getCiriKhusus().isEmpty()
                && a.getCiriKhusus().equalsIgnoreCase(b.getCiriKhusus())) skor += 20;
        else if (!a.getCiriKhusus().isEmpty() && !b.getCiriKhusus().isEmpty()
                && kemiripanTeks(a.getCiriKhusus(), b.getCiriKhusus()) >= 0.5) skor += 10;
        return Math.min(skor, 100);
    }

    /** Kemiripan sederhana berbasis irisan kata. */
    private double kemiripanTeks(String x, String y) {
        String[] xs = x.toLowerCase().split("\\s+");
        String[] ys = y.toLowerCase().split("\\s+");
        int cocok = 0;
        for (String w1 : xs)
            for (String w2 : ys)
                if (w1.equals(w2)) { cocok++; break; }
        int total = Math.max(xs.length, ys.length);
        return total == 0 ? 0 : (double) cocok / total;
    }

    /** Cari rekomendasi temuan untuk satu laporan hilang; kirim notifikasi bila >= ambang. */
    public List<Laporan> cariRekomendasi(Laporan hilang, List<Laporan> daftarLaporan) {
        List<Laporan> hasil = new ArrayList<>();
        boolean notifTerkirim = false;
        for (Laporan t : daftarLaporan) {
            if (t.getStatus() == StatusLaporan.DIKEMBALIKAN || t.getStatus() == StatusLaporan.DIARSIPKAN)
                continue;
            int skor = hitungSkor(hilang, t);
            if (skor >= AMBANG_REKOMENDASI) {
                hasil.add(t);
                // satu notifikasi per proses pencocokan agar tidak spam
                if (!notifTerkirim && hilang.getPelapor() instanceof Mahasiswa m) {
                    String pesan = "=== NOTIFIKASI KECOCOKAN ===\n"
                            + "Barang Anda kemungkinan ditemukan.\n"
                            + t.tampilkanKePublik() + "\n"
                            + "Skor kecocokan: " + skor + "/100";
                    new Notifikasi(m, pesan).kirimNotif();
                    notifTerkirim = true;
                }
                if (hilang.getStatus() != StatusLaporan.COCOK) {
                    hilang.ubahStatus(StatusLaporan.COCOK);
                }
            }
        }
        hasil.sort(Comparator.comparingInt((Laporan t) -> hitungSkor(hilang, t)).reversed());
        return hasil;
    }

    /** Jalankan auto-matching seluruh laporan HILANG terhadap daftar laporan. */
    public void jalankanAutoMatching(List<Laporan> semuaLaporan) {
        List<Laporan> hilang = semuaLaporan.stream()
                .filter(l -> l.getJenis() == JenisLaporan.HILANG)
                .filter(l -> l.getStatus() != StatusLaporan.DIKEMBALIKAN)
                .toList();
        for (Laporan h : hilang) {
            // lewati bila sudah pernah ditemukan kandidat cocok (hindari notifikasi duplikat)
            if (h.getStatus() == StatusLaporan.COCOK) continue;
            cariRekomendasi(h, semuaLaporan);
        }
    }
}
