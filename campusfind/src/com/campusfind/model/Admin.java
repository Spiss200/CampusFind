package com.campusfind.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin: master data (lokasi & kategori), audit log, statistik,
 * dan pengarsipan barang lama.
 */
public class Admin extends User implements Pelapor {
    private final List<String> daftarLokasi = new ArrayList<>();
    private final List<String> daftarKategori = new ArrayList<>();
    private final List<String> auditLog = new ArrayList<>();

    public Admin(int id, String nama, String email, String password) {
        super(id, nama, email, password);
        daftarLokasi.add("Ruang Kelas Gedung A");
        daftarLokasi.add("Perpustakaan");
        daftarLokasi.add("Kantin");
        daftarLokasi.add("Laboratorium");
        daftarLokasi.add("Parkiran Timur");
        daftarKategori.add("Elektronik");
        daftarKategori.add("Aksesoris Pribadi");
        daftarKategori.add("Dokumen");
        daftarKategori.add("Tas");
    }

    @Override
    public void buatLaporan(Laporan l) {
        // admin juga warga kampus, boleh melapor
    }

    public void kelolaMasterData(boolean tambah, String jenisData, String nilai) {
        if ("LOKASI".equalsIgnoreCase(jenisData)) {
            if (tambah) daftarLokasi.add(nilai); else daftarLokasi.remove(nilai);
        } else if ("KATEGORI".equalsIgnoreCase(jenisData)) {
            if (tambah) daftarKategori.add(nilai); else daftarKategori.remove(nilai);
        }
        catatAudit("Master data " + jenisData + ": " + (tambah ? "tambah " : "hapus ") + nilai);
    }

    public void catatAudit(String aktivitas) {
        auditLog.add(java.time.LocalDateTime.now() + " | " + aktivitas);
    }

    public void lihatAuditLog() {
        if (auditLog.isEmpty()) System.out.println("(Audit log kosong)");
        for (String a : auditLog) System.out.println(a);
    }

    /** Arsipkan laporan TEMUAN yang melebihi batas hari tanpa klaim selesai. */
    public int arsipkanBarangLama(List<Laporan> semua, int batasHari) {
        int dinarsipkan = 0;
        for (Laporan l : semua) {
            boolean belumSelesai = l.getStatus() != StatusLaporan.DIKEMBALIKAN
                    && l.getStatus() != StatusLaporan.DIARSIPKAN;
            if (belumSelesai && l.umurHari() >= batasHari) {
                l.ubahStatus(StatusLaporan.DIARSIPKAN);
                catatAudit("Arsip otomatis laporan #" + l.getId());
                dinarsipkan++;
            }
        }
        return dinarsipkan;
    }

    public String lihatStatistik(List<Laporan> semua, List<Klaim> semuaKlaim) {
        long hilang = semua.stream().filter(l -> l.getJenis() == JenisLaporan.HILANG).count();
        long temuan = semua.stream().filter(l -> l.getJenis() == JenisLaporan.TEMUAN).count();
        long kembali = semua.stream().filter(l -> l.getStatus() == StatusLaporan.DIKEMBALIKAN).count();
        double tingkat = temuan == 0 ? 0 : 100.0 * kembali / temuan;
        return String.format("""
                === STATISTIK CAMPUSFIND ===
                Laporan HILANG   : %d
                Laporan TEMUAN   : %d
                Klaim diajukan   : %d
                Barang kembali   : %d
                Tingkat kembali  : %.1f%%
                """, hilang, temuan, semuaKlaim.size(), kembali, tingkat);
    }

    public List<String> getDaftarLokasi() {
        return daftarLokasi;
    }

    public List<String> getDaftarKategori() {
        return daftarKategori;
    }

    @Override
    public String getPeran() {
        return "Admin";
    }
}
