package com.campusfind.model;

/**
 * Barang elektronik: bukti utama berbasis akun terdaftar (OTP/unlock/recovery),
 * nomor seri & dus menjadi bukti cadangan.
 */
public class BarangElektronik extends Item {
    private String merk;
    private String tipe;
    private String akunTerdaftar; // email/no HP yang login di perangkat
    private String nomorSeri;     // opsional, bukti cadangan

    public BarangElektronik(int id, String nama, String kategori, String warna, String ciriKhusus,
                            String merk, String tipe, String akunTerdaftar, String nomorSeri) {
        super(id, nama, kategori, warna, ciriKhusus);
        this.merk = merk;
        this.tipe = tipe;
        this.akunTerdaftar = akunTerdaftar;
        this.nomorSeri = nomorSeri == null ? "" : nomorSeri;
    }

    @Override
    public void tampilkanDetail() {
        System.out.println("Barang     : " + nama + " (" + merk + " " + tipe + ")");
        System.out.println("Kategori   : " + kategori);
        System.out.println("Warna      : " + warna);
        System.out.println("Ciri Khusus: " + ciriKhusus);
        System.out.println("Akun Terdaf: " + samarkan(akunTerdaftar));
    }

    /** Samarkan sebagian akun agar privasi pemilik terjaga. */
    public static String samarkan(String akun) {
        if (akun == null || akun.isBlank()) return "(tidak terdaftar)";
        int at = akun.indexOf('@');
        if (at > 2) {
            return akun.substring(0, 2) + "***" + akun.substring(at);
        }
        return akun.substring(0, Math.min(3, akun.length())) + "****";
    }

    @Override
    public JenisBukti[] getSyaratBuktiUtama() {
        return new JenisBukti[]{
                JenisBukti.OTP_AKUN_TERDAFTAR,
                JenisBukti.UNLOCK_PERANGKAT,
                JenisBukti.RECOVERY_AKUN
        };
    }

    public JenisBukti[] getBuktiCadangan() {
        return new JenisBukti[]{JenisBukti.NOMOR_SERI, JenisBukti.DUS_PEMBELIAN};
    }

    public String getMerk() {
        return merk;
    }

    public String getTipe() {
        return tipe;
    }

    public String getAkunTerdaftar() {
        return akunTerdaftar;
    }

    public String getNomorSeri() {
        return nomorSeri;
    }
}
