package com.campusfind.model;

/**
 * Class induk semua barang (abstraction).
 * Barang hanya menyimpan data, bukan logika pencocokan.
 */
public abstract class Item {
    protected int id;
    protected String nama;
    protected String kategori;
    protected String warna;
    protected String ciriKhusus;

    public Item(int id, String nama, String kategori, String warna, String ciriKhusus) {
        this.id = id;
        this.nama = nama;
        this.kategori = kategori;
        this.warna = warna;
        this.ciriKhusus = ciriKhusus == null ? "" : ciriKhusus;
    }

    /** Polymorphism: perilaku tampilan berbeda sesuai jenis barang. */
    public abstract void tampilkanDetail();

    /** Polymorphism: syarat bukti utama berbeda sesuai jenis barang. */
    public abstract JenisBukti[] getSyaratBuktiUtama();

    /** Kode singkat untuk QR/tracking. */
    public String getIdentitasSingkat() {
        String n = nama.toUpperCase().replaceAll("[^A-Z0-9]", "");
        return (n.isEmpty() ? "ITEM" : n.substring(0, Math.min(4, n.length()))) + id;
    }

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getKategori() {
        return kategori;
    }

    public String getWarna() {
        return warna;
    }

    public String getCiriKhusus() {
        return ciriKhusus;
    }
}
