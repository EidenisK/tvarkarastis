package com.example.user.myapplication;

public class Mokinys {
    private String vardas, nuoroda;
    private boolean pazymetas = false;

    public Mokinys() {}
    public Mokinys(String vardas, String nuoroda) {
        this.vardas = vardas;
        this.nuoroda = nuoroda;
    }

    public Mokinys(Mokinys m) {
        vardas = m.vardas;
        nuoroda = m.nuoroda;
        pazymetas = m.pazymetas;
    }

    public String getVardas() {
        return vardas;
    }
    public void setVardas(String vardas) {
        this.vardas = vardas;
    }

    public String getNuoroda() {
        return nuoroda;
    }
    public void setNuoroda(String nuoroda) {
        this.nuoroda = nuoroda;
    }

    public boolean getPazymetas() {return pazymetas;}
    public void setPazymetas(boolean pazymetas) {this.pazymetas = pazymetas;}
}
