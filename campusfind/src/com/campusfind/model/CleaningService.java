package com.campusfind.model;

/**
 * Cleaning Service: ujung tombak penemu barang di lapangan.
 * Boleh membuat laporan temuan, tetapi TIDAK punya hak verifikasi klaim.
 */
public class CleaningService extends Petugas {

    public CleaningService(int id, String nama, String email, String password) {
        super(id, nama, email, password, JenisPetugas.CLEANING_SERVICE);
    }

    /** Mencatat penyerahan fisik barang dari CS ke pos Security. */
    public String serahkanBarangKePos(Laporan l, Security penerima) {
        l.ubahStatus(StatusLaporan.DIVERIFIKASI);
        String kode = penerima.generateQR(l);
        return "Barang \"" + l.getItem().getNama() + "\" diserahkan ke pos Security "
                + penerima.getNama() + ". Kode tracking: " + kode;
    }
}
