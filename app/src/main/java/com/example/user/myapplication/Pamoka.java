package com.example.user.myapplication;

public class Pamoka
{
    public String pavadinimas = "";
    private String mokytojai = "";
    public String laikas = "";
    public int numeris = 0;

    int int_laikas[][] = new int[2][2];
    int sav_diena;

    private String nuoroda = "";

    void setPavadinimas(String pvd) {
        pavadinimas = pvd;
    }
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

    String nuoroda() { return nuoroda; }
    void nuoroda(String s) {
        nuoroda = s;
    }

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

    void getIntLaikas()
    {
        String temp_string_laikas[] = laikas.split("[:-]");
        for(int i = 0; i < 2; i++)
            for(int j = 0; j < 2; j++)
                int_laikas[i][j] = Integer.parseInt(temp_string_laikas[i*2+j]);
    }
}
