package com.campusfind.model;

import java.util.ArrayList;
import java.util.List;

/** Class induk petugas (Security / Cleaning Service). */
public abstract class Petugas extends User implements Pelapor {
    protected JenisPetugas jenis;
    protected final List<Laporan> laporanSaya = new ArrayList<>();

    public Petugas(int id, String nama, String email, String password, JenisPetugas jenis) {
        super(id, nama, email, password);
        this.jenis = jenis;
    }

    /** Semua petugas boleh membuat laporan temuan dari lapangan. */
    @Override
    public void buatLaporan(Laporan l) {
        laporanSaya.add(l);
    }

    public JenisPetugas getJenis() {
        return jenis;
    }

    public List<Laporan> getLaporanSaya() {
        return laporanSaya;
    }

    @Override
    public String getPeran() {
        return jenis.getLabel();
    }
}
