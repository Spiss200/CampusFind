package com.campusfind.model;

/** Status laporan dengan validasi transisi (encapsulation + enum). */
public enum StatusLaporan {
    DILAPORKAN("Dilaporkan"),
    DIVERIFIKASI("Terverifikasi"),
    COCOK("Ada Kecocokan"),
    DALAM_PROSES_KLAIM("Dalam Proses Klaim"),
    DIKEMBALIKAN("Dikembalikan"),
    DITOLAK("Ditolak"),
    DIARSIPKAN("Diarsipkan");

    private final String label;

    StatusLaporan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Menjamin alur status selalu valid dan dapat diaudit. */
    public boolean bolehKe(StatusLaporan target) {
        switch (this) {
            case DILAPORKAN:
                return target == DIVERIFIKASI || target == COCOK
                        || target == DALAM_PROSES_KLAIM || target == DIARSIPKAN;
            case DIVERIFIKASI:
                return target == COCOK || target == DALAM_PROSES_KLAIM || target == DIARSIPKAN;
            case COCOK:
                return target == DALAM_PROSES_KLAIM || target == DIARSIPKAN;
            case DALAM_PROSES_KLAIM:
                return target == DIKEMBALIKAN || target == DITOLAK;
            default: // DIKEMBALIKAN, DITOLAK, DIARSIPKAN adalah status akhir relatif
                return target == DIARSIPKAN && this != DIKEMBALIKAN;
        }
    }
}
