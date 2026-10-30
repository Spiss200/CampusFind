package com.campusfind.model;

/**
 * Barang non-elektronik (dompet, tas, kunci, dokumen):
 * diverifikasi melalui ciri fisik dan isi barang.
 */
public class BarangNonElektronik extends Item {
    private String deskripsiIsi; // detail isi barang (hanya diketahui pemilik sah)

    public BarangNonElektronik(int id, String nama, String kategori, String warna,
                               String ciriKhusus, String deskripsiIsi) {
        super(id, nama, kategori, warna, ciriKhusus);
        this.deskripsiIsi = deskripsiIsi == null ? "" : deskripsiIsi;
    }

    @Override
    public void tampilkanDetail() {
        System.out.println("Barang     : " + nama);
        System.out.println("Kategori   : " + kategori);
        System.out.println("Warna      : " + warna);
        System.out.println("Ciri Khusus: " + ciriKhusus);
        // deskripsiIsi sengaja TIDAK ditampilkan ke publik -> privasi bukti
    }

    @Override
    public JenisBukti[] getSyaratBuktiUtama() {
        return new JenisBukti[]{JenisBukti.CIRI_FISIK};
    }

    /** Hanya petugas berwenang yang boleh membandingkan isi sebenarnya. */
    public boolean cocokDenganIsi(String claimedIsi) {
        if (claimedIsi == null || claimedIsi.isBlank()) return false;
        String c = claimedIsi.toLowerCase();
        for (String kata : deskripsiIsi.toLowerCase().split("[,;]")) {
            String k = kata.trim();
            if (k.length() > 3 && c.contains(k)) return true;
        }
        return false;
    }

    public String getDeskripsiIsi() {
        return deskripsiIsi;
    }
}
