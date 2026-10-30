package com.campusfind.model;

/** Siapa pun (mahasiswa, petugas, admin) boleh membuat laporan. */
public interface Pelapor {
    void buatLaporan(Laporan l);
}
