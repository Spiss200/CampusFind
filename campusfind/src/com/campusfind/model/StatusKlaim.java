package com.campusfind.model;

/** Status proses klaim. */
public enum StatusKlaim {
    MENUNGGU("Menunggu Verifikasi"),
    DIVERIFIKASI("Bukti Terverifikasi"),
    DISETUJUI("Disetujui"),
    DITOLAK("Ditolak");

    private final String label;

    StatusKlaim(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
