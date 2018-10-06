package com.example.user.myapplication;

public class Tvarkarastis {
    String pavadinimas, klase;
    String [] laikas = new String[9];
    int [][] intLaikas = new int[18][2];
    int intLaikoSuma = 0;
    Pamoka [][] pamokos = new Pamoka[5][9];
    int maxPamoku = 0;

    Tvarkarastis() {
        pavadinimas = "";
        klase = "";
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 9; j++) {
                pamokos[i][j] = new Pamoka();
            }
        }
        for(int x = 0; x < 9; x++)
            laikas[x] = "";
        maxPamoku = 0;
    }

    Tvarkarastis(String pavadinimas) {
        this.pavadinimas = pavadinimas;
    }

    public void clear() { //resset all variables
        pavadinimas = "";
        klase = "";
        maxPamoku = 0;
        for(int i = 0; i < 5; i++)
            for(int j = 0; j < 9; j++) {
                pamokos[i][j].clear();
            }
    }
}
