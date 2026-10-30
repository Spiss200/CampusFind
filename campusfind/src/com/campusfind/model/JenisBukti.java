package com.campusfind.model;

/** Jenis bukti kepemilikan barang. */
public enum JenisBukti {
    OTP_AKUN_TERDAFTAR("OTP ke akun terdaftar pada perangkat"),
    UNLOCK_PERANGKAT("Membuka kunci perangkat di depan petugas"),
    RECOVERY_AKUN("Jalur recovery akun bila lupa kata sandi"),
    NOMOR_SERI("Menyebutkan nomor seri (bukti cadangan)"),
    DUS_PEMBELIAN("Menunjukkan dus/struk pembelian (bukti cadangan)"),
    CIRI_FISIK("Menyebutkan ciri fisik & isi barang");

    private final String deskripsi;

    JenisBukti(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
}
