package com.campusfind.model;

import java.util.ArrayList;
import java.util.List;

/** Pengguna mahasiswa: melapor, melihat rekomendasi, mengajukan klaim. */
public class Mahasiswa extends User implements Pelapor {
    private String nim;
    private final List<Laporan> laporanSaya = new ArrayList<>();
    private final List<Klaim> klaimSaya = new ArrayList<>();
    private final List<Notifikasi> notifikasiSaya = new ArrayList<>();

    public Mahasiswa(int id, String nama, String email, String password, String nim) {
        super(id, nama, email, password);
        this.nim = nim;
    }

    @Override
    public void buatLaporan(Laporan l) {
        laporanSaya.add(l);
    }

    public void ajukanKlaim(Klaim k) {
        klaimSaya.add(k);
    }

    public void tambahNotifikasi(Notifikasi n) {
        notifikasiSaya.add(n);
    }

    public String tampilDashboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("== Dashboard Mahasiswa: ").append(nama)
          .append(" (NIM ").append(nim).append(") ==\n");
        if (laporanSaya.isEmpty()) sb.append("(Belum ada laporan yang Anda buat)\n");
        for (Laporan l : laporanSaya) {
            sb.append("- #").append(l.getId()).append(" [")
              .append(l.getJenis().getLabel()).append("] ")
              .append(l.getItem().getNama())
              .append(" | Status: ").append(l.getStatus().getLabel()).append("\n");
        }
        if (!klaimSaya.isEmpty()) {
            sb.append("Klaim Anda:\n");
            for (Klaim k : klaimSaya) {
                sb.append("- Klaim #").append(k.getId())
                  .append(" -> Laporan TEMUAN #").append(k.getLaporanTemuan().getId())
                  .append(" | ").append(k.getStatus().getLabel()).append("\n");
            }
        }
        if (!notifikasiSaya.isEmpty()) {
            sb.append("Notifikasi:\n");
            for (Notifikasi n : notifikasiSaya) {
                sb.append(n).append("\n");
            }
        }
        return sb.toString();
    }

    public String getNim() {
        return nim;
    }

    public List<Laporan> getLaporanSaya() {
        return laporanSaya;
    }

    public List<Klaim> getKlaimSaya() {
        return klaimSaya;
    }

    @Override
    public String getPeran() {
        return "Mahasiswa";
    }
}
