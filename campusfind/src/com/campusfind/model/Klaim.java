package com.campusfind.model;

import java.util.Random;

/**
 * Class Klaim: mengelola bukti dan proses verifikasi kepemilikan
 * dengan pola challenge-response serta kunci anti klaim ganda.
 */
public class Klaim {
    private int id;
    private final Mahasiswa pengklaim;
    private final Laporan laporanTemuan;
    private StatusKlaim status = StatusKlaim.MENUNGGU;
    private String kodeOTP;
    private final String deskripsiBukti;
    private final Random rng = new Random();

    public Klaim(int id, Mahasiswa pengklaim, Laporan laporanTemuan, String deskripsiBukti) {
        this.id = id;
        this.pengklaim = pengklaim;
        this.laporanTemuan = laporanTemuan;
        this.deskripsiBukti = deskripsiBukti;
    }

    /** Kunci klaim: cegah klaim ganda pada satu barang. */
    public boolean ajukan() {
        StatusLaporan s = laporanTemuan.getStatus();
        if (s == StatusLaporan.DALAM_PROSES_KLAIM) {
            return false; // sudah ada klaim lain yang sedang diverifikasi
        }
        if (s == StatusLaporan.DIKEMBALIKAN || s == StatusLaporan.DIARSIPKAN) {
            return false; // barang sudah selesai / diarsipkan
        }
        // DITOLAK boleh diajukan kembali oleh klaim baru
        laporanTemuan.ubahStatus(StatusLaporan.DALAM_PROSES_KLAIM);
        return true;
    }

    /** Simulasi pengiriman OTP ke akun/nomor terdaftar pada perangkat. */
    public String kirimOTP() {
        kodeOTP = String.format("%06d", 100000 + rng.nextInt(900000));
        return kodeOTP;
    }

    /** Pemilik membuktikan penguasaan akun terdaftar. */
    public boolean cekOTP(String input) {
        return kodeOTP != null && kodeOTP.equals(input.trim());
    }

    public void ubahStatusKlaim(StatusKlaim s) {
        status = s;
    }

    public int getId() {
        return id;
    }

    public Mahasiswa getPengklaim() {
        return pengklaim;
    }

    public Laporan getLaporanTemuan() {
        return laporanTemuan;
    }

    public StatusKlaim getStatus() {
        return status;
    }

    public String getDeskripsiBukti() {
        return deskripsiBukti;
    }
}
