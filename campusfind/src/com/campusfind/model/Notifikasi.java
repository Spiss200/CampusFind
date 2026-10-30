package com.campusfind.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Pemberitahuan kepada pengguna terkait. */
public class Notifikasi {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static int counter = 0;

    private final int id;
    private final User tujuan;
    private final String pesan;
    private final String waktu;
    private boolean dibaca = false;

    public Notifikasi(User tujuan, String pesan) {
        this.id = ++counter;
        this.tujuan = tujuan;
        this.pesan = pesan;
        this.waktu = LocalDateTime.now().format(FMT);
    }

    /** Kirim notifikasi ke pengguna tujuan (simulasi in-app). */
    public void kirimNotif() {
        if (tujuan instanceof Mahasiswa m) {
            m.tambahNotifikasi(this);
        }
        System.out.println("[NOTIF #" + id + " -> " + tujuan.getNama() + "] " + pesan);
    }

    public void tandaiDibaca() {
        dibaca = true;
    }

    @Override
    public String toString() {
        return "- (" + waktu + ") " + pesan + (dibaca ? " [dibaca]" : " [baru]");
    }
}
