package com.example.user.myapplication;

public class Pamoka
{
    public String pavadinimas = "";
    private String mokytojai = "";
    public String laikas = "";
    public int numeris = 0;

    void setPavadinimas(String pvd) {
        pavadinimas = pvd;
    }
    void addMokytojai(String add) {mokytojai += add;}
    void setLaikas(String lks) {
        laikas = lks;
    }
    void setNumeris(int n) {numeris = n;}

    String getPavadinimas() {
        return pavadinimas;
    }
    String getMokytojai() {
        return mokytojai;
    }
    String getLaikas() {
        return laikas;
    }
    int getNumeris() { return numeris; }

    Pamoka() {}
    Pamoka(String s) {
        pavadinimas = s;
    }

    Pamoka(String pavadinimas, String mokytojai, String laikas, int numeris) {
        this.pavadinimas = pavadinimas;
        this.mokytojai = mokytojai;
        this.laikas = laikas;
        this.numeris = numeris;
    }

    void clear() {
        pavadinimas = "";
        mokytojai = "";
        laikas = "";
        numeris = 0;
    }
}
