package com.campusfind.model;

/** Hak memutuskan klaim hanya dimiliki pihak berwenang (Security). */
public interface Verifikator {
    void verifikasiKlaim(Klaim k, boolean disetujui);
}
