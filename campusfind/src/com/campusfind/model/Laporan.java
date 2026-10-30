package com.campusfind.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Class Laporan: wadah kejadian yang menghubungkan pelapor (User),
 * barang (Item), lokasi, waktu, dan status. Menggunakan komposisi.
 */
public class Laporan {
    private int id;
    private final User pelapor;      // komposisi: laporan memiliki pelapor
    private final Item item;         // komposisi: laporan memiliki barang
    private final JenisLaporan jenis;
    private final String lokasi;
    private final LocalDateTime waktu;
    private StatusLaporan status = StatusLaporan.DILAPORKAN;
    private String kodeQR;           // hanya diisi untuk laporan TEMUAN

    public Laporan(int id, User pelapor, Item item, JenisLaporan jenis,
                   String lokasi, LocalDateTime waktu) {
        this.id = id;
        this.pelapor = pelapor;
        this.item = item;
        this.jenis = jenis;
        this.lokasi = lokasi;
        this.waktu = waktu;
    }

    /** Ubah status dengan validasi transisi enum (encapsulation). */
    public boolean ubahStatus(StatusLaporan baru) {
        if (!status.bolehKe(baru)) {
            System.out.println("  [DITOLAK] Transisi status tidak valid: "
                    + status.getLabel() + " -> " + baru.getLabel());
            return false;
        }
        status = baru;
        return true;
    }

    /** Privasi: nama pelapor disembunyikan dari pengguna lain. */
    public String tampilkanKePublik() {
        return item.getNama() + " (" + item.getWarna() + ") di " + lokasi
                + " - " + jenis.getLabel();
    }

    /** Versi lengkap hanya untuk petugas berwenang. */
    public String tampilkanLengkap() {
        StringBuilder sb = new StringBuilder();
        sb.append("Laporan #").append(id).append(" [").append(jenis.getLabel()).append("]\n");
        sb.append("  Barang : ").append(item.getNama())
          .append(" (").append(item.getKategori()).append(", ").append(item.getWarna()).append(")\n");
        sb.append("  Ciri   : ").append(item.getCiriKhusus()).append("\n");
        sb.append("  Lokasi : ").append(lokasi).append("\n");
        sb.append("  Waktu  : ").append(waktu).append("\n");
        sb.append("  Status : ").append(status.getLabel()).append("\n");
        sb.append("  Pelapor: ").append(pelapor.getNama())
          .append(" (").append(pelapor.getPeran()).append(")\n");
        if (kodeQR != null) sb.append("  QR     : ").append(kodeQR).append("\n");
        return sb.toString();
    }

    public long umurHari() {
        return ChronoUnit.DAYS.between(waktu, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public User getPelapor() {
        return pelapor;
    }

    public Item getItem() {
        return item;
    }

    public JenisLaporan getJenis() {
        return jenis;
    }

    public String getLokasi() {
        return lokasi;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public StatusLaporan getStatus() {
        return status;
    }

    public String getKodeQR() {
        return kodeQR;
    }

    public void setKodeQR(String kodeQR) {
        this.kodeQR = kodeQR;
    }
}
