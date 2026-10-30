package com.campusfind.model;

/**
 * Class induk semua pengguna sistem (abstraction + inheritance).
 * Data akun dilindungi dengan encapsulation.
 */
public abstract class User {
    protected int id;
    protected String nama;
    protected String email;
    protected String password;
    protected boolean sesiAktif = false;

    public User(int id, String nama, String email, String password) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.password = password;
    }

    /** Autentikasi kredensial sederhana. */
    public boolean login(String email, String password) {
        if (this.email.equalsIgnoreCase(email) && this.password.equals(password)) {
            sesiAktif = true;
            return true;
        }
        return false;
    }

    public void logout() {
        sesiAktif = false;
    }

    public boolean isSesiAktif() {
        return sesiAktif;
    }

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    /** Peran tampilan untuk menu aplikasi. */
    public abstract String getPeran();
}
