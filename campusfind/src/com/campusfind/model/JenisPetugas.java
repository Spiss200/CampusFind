package com.campusfind.model;

/** Jenis petugas (cleaning service / security). */
public enum JenisPetugas {
    SECURITY("Security"),
    CLEANING_SERVICE("Cleaning Service");

    private final String label;

    JenisPetugas(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
