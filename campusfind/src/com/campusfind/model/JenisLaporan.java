package com.campusfind.model;

/** Jenis kejadian laporan. */
public enum JenisLaporan {
    HILANG("Kehilangan"),
    TEMUAN("Barang Temuan");

    private final String label;

    JenisLaporan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
