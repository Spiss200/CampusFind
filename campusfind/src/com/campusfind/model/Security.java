package com.campusfind.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Security: menerima barang di pos, memverifikasi klaim,
 * mengubah status, mencetak QR Code, mencatat serah terima.
 * Satu-satunya peran yang mengimplementasikan Verifikator.
 */
public class Security extends Petugas implements Verifikator {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Security(int id, String nama, String email, String password) {
        super(id, nama, email, password, JenisPetugas.SECURITY);
    }

    /** Hak memutuskan klaim (polymorphism via interface Verifikator). */
    @Override
    public void verifikasiKlaim(Klaim k, boolean disetujui) {
        Laporan l = k.getLaporanTemuan();
        if (disetujui) {
            k.ubahStatusKlaim(StatusKlaim.DIVERIFIKASI);
            k.ubahStatusKlaim(StatusKlaim.DISETUJUI);
            l.ubahStatus(StatusLaporan.DIKEMBALIKAN);
        } else {
            k.ubahStatusKlaim(StatusKlaim.DITOLAK);
            // buka kunci laporan agar pihak lain masih berpeluang mengklaim
            l.ubahStatus(StatusLaporan.DITOLAK);
        }
    }

    public void updateStatus(Laporan l, StatusLaporan s) {
        l.ubahStatus(s);
    }

    /** Kode unik untuk pelacakan fisik; dapat dikonversi menjadi gambar QR. */
    public String generateQR(Laporan l) {
        String kode = "FIND-" + String.format("%04d", l.getId()) + "-" + l.getItem().getIdentitasSingkat();
        l.setKodeQR(kode);
        return kode;
    }

    /** Mencetak representasi teks QR sederhana. */
    public String cetakQR(Laporan l) {
        String kode = l.getKodeQR() != null ? l.getKodeQR() : generateQR(l);
        StringBuilder sb = new StringBuilder();
        sb.append("+----------------------------------------+\n");
        sb.append("|  [QR CODE]  ").append(kode).append('\n');
        sb.append("|  Barang   : ").append(rapikan(l.getItem().getNama(), 29)).append('\n');
        sb.append("|  Lokasi   : ").append(rapikan(l.getLokasi(), 29)).append('\n');
        sb.append("|  Diterima : ").append(LocalDateTime.now().format(FMT))
          .append(" oleh ").append(rapikan(nama, 12)).append('\n');
        sb.append("+----------------------------------------+\n");
        return sb.toString();
    }

    private String rapikan(String s, int w) {
        if (s == null) s = "";
        s = s.length() > w ? s.substring(0, w - 1) : s;
        return String.format("%-" + w + "s", s);
    }
}
