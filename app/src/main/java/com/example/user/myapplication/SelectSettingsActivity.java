package com.example.user.myapplication;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SelectSettingsActivity extends AppCompatActivity {

    LinearLayout priminimai_main, apie_main;
    ConstraintLayout vardai_main;
    ImageView vardai_button, priminimai_button, apie_button;
    SharedPreferences mPrefs;
    boolean priminimai, vibracija, panaikinimai;
    int informacija;

    public List<Mokinys> mokiniai = new ArrayList<>();
    public List<Mokinys> rodomiMokiniai = new ArrayList<>();

    MokiniaiAdapter mAdapter;

    String string;
    EditText inputName;
    boolean switched;

    boolean keite_nustatymus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = getSharedPreferences("label", 0);
        if(mPrefs.getBoolean("darkTheme", false))
            setTheme(R.style.DarkTheme);

        setContentView(R.layout.activity_select_settings);

        priminimai_main = findViewById(R.id.priminimai_main);
        vardai_main = findViewById(R.id.vardai_main);
        apie_main = findViewById(R.id.apie_main);

        priminimai_button = findViewById(R.id.alarmSettings);
        vardai_button = findViewById(R.id.pasirinktiMokini);
        apie_button = findViewById(R.id.apie);

        if(mPrefs.getBoolean("darkTheme", false)) {
            vardai_button.setImageDrawable(getDrawable(R.drawable.pasirinkti_mokini_white));
            priminimai_button.setImageDrawable(getDrawable(R.drawable.alarm_settings_white));
            apie_button.setImageDrawable(getDrawable(R.drawable.apie_white));
        }

        rodytiIssaugotaSarasa();
    }

    private void rodytiIssaugotaSarasa() {
        String sarasas = mPrefs.getString("nameString", "NULL"), tekstas = mPrefs.getString("issaugotoSarasoTekstas", "NULL");
        if(!sarasas.equals("NULL") && sarasas.equals(tekstas) && mPrefs.getBoolean("switched", false) == mPrefs.getBoolean("issaugotoSarasoSwitched", false)) {
            mokiniai.clear();
            rodomiMokiniai.clear();
            mokiniai = Funkcijos.getList(mPrefs, "mokiniuSarasas");
            rodomiMokiniai = new ArrayList<>(mokiniai);
        }
    }

    public void changeTheme(View view) {
        boolean dark = mPrefs.getBoolean("darkTheme", false);
        dark = !dark;
        mPrefs.edit().putBoolean("darkTheme", dark).apply();

        recreate();
    }




    /*----------------- Perjungti nustatymų skirtukus -------------------------------------*/
    /**Funkcija, atidaranti mokinio pasirinkimo meniu*/
    public void pasirinkti(View view) {

        inputName = findViewById(R.id.inputName);
        inputName.addTextChangedListener(filterTextWatcher);

        switched = mPrefs.getBoolean("switched", false);

        nustatytiSarasa();
        String tempVardai = mPrefs.getString("nameString", "NULL");
        if(mokiniai.size() > 0) {
            mAdapter.notifyDataSetChanged();
            inputName.setHint(R.string.iveskite_varda);
        } else if(!tempVardai.equals("NULL"))
            analyzeNames(tempVardai);

        mAdapter.notifyDataSetChanged();

        resetColors(view);
        resetSelection(vardai_main);
    }

    /**Funkcija, atidaranti priminimų nustatymų meniu*/
    public void priminimai(View view) {
        uzkrautiNustatymus();
        resetColors(view);
        resetSelection(priminimai_main);
        setPriminimaiTextProperties(priminimai);
    }

    void uzkrautiNustatymus() {
        priminimai = mPrefs.getBoolean("priminimai", false);
        vibracija = mPrefs.getBoolean("vibracija", true);
        informacija = mPrefs.getInt("informacija", 0);

        panaikinimai = mPrefs.getBoolean("panaikinimai", false);

        setPriminimaiTextProperties(priminimai);
        changeSettingColor(vibracija, R.id.vibracijosBusena);
        setInformacijaTextProperties(informacija);
        setPanaikinimaiTextProperties();
        setNukirpimoTextProperties();
        changeSettingColor(mPrefs.getBoolean("autoUpdate", true), R.id.autoUpdateBusena);
        changeSettingColor(mPrefs.getBoolean("versionUpdate", true), R.id.versionUpdateBusena);
    }

    /**Funkcija, atidaranti "Apie" meniu*/
    public void apie(View view) {
        resetColors(view);
        resetSelection(apie_main);
    }

    /**Funkcija, padaranti matomą tik nurodytą nustatymų dalį*/
    void resetSelection(LinearLayout ll) {
        apie_main.setVisibility(View.GONE);
        vardai_main.setVisibility(View.GONE);
        priminimai_main.setVisibility(View.GONE);

        ll.setVisibility(View.VISIBLE);
    }
    void resetSelection(ConstraintLayout ll) {
        apie_main.setVisibility(View.GONE);
        vardai_main.setVisibility(View.GONE);
        priminimai_main.setVisibility(View.GONE);

        ll.setVisibility(View.VISIBLE);
    }

    /**Funkcija, tamsesne spalva pažyminti nurodytą nustatymų dalį*/
    void resetColors(View view) {
        boolean dark = mPrefs.getBoolean("darkTheme", false);
        int col = (!dark) ? ContextCompat.getColor(getApplicationContext(), R.color.defaultItemColor) : ContextCompat.getColor(getApplicationContext(), R.color.defaultItemColor1);
        apie_button.setBackgroundColor(col);
        vardai_button.setBackgroundColor(col);
        priminimai_button.setBackgroundColor(col);

        col = (!dark) ? ContextCompat.getColor(getApplicationContext(), R.color.selectedItemColor) : ContextCompat.getColor(getApplicationContext(), R.color.selectedItemColor1);
        view.setBackgroundColor(col);
    }
    /*--------------------------------------------------------------------------------------*/




    /*------------------------- Išeiti iš nustatymų meniu ----------------------------------*/
    /**Funkcija grįžti į pradinį ekraną**/
    public void returnToMenu(View view) {
        exitActivity();
    }

    @Override
    public void onBackPressed() {
        exitActivity();
    }

    void exitActivity() {
        issaugotiMokinius(mokiniai);
        mPrefs.edit().putBoolean("switched", switched).apply();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("keite_nustatymus", keite_nustatymus);
        returnIntent.putExtra("needs_restart", mPrefs.getBoolean("darkTheme", false) != mPrefs.getBoolean("mainActivityDarkTheme", false));
        setResult(RESULT_OK, returnIntent);

        finish();
    }
    /*--------------------------------------------------------------------------------------*/




    /*----------------- Pakeisti priminimų ir kt. nustatymus -------------------------------*/
    void changeSettingColor(boolean ijungta, int ID) {
        TextView busena = findViewById(ID);
        String text;
        int col;
        boolean dark = mPrefs.getBoolean("darkTheme", false);

        if(ijungta) {
            text = "Įjungta";
            col = (!dark) ? ContextCompat.getColor(getApplicationContext(), R.color.greenText) : ContextCompat.getColor(getApplicationContext(), R.color.greenText1);
        } else {
            text = "Išjungta";
            col = (!dark) ? ContextCompat.getColor(getApplicationContext(), R.color.redText) : ContextCompat.getColor(getApplicationContext(), R.color.redText1);
        }

        busena.setText(text);
        busena.setTextColor(col);
    }


    /*--------------------------- Priminimai ----------------------------*/
    /**Funkcija, įjungianti ar išjungianti priminimus*/
    public void pakeistiPriminimuNustatymus(View view) {
        keite_nustatymus = true;
        priminimai = mPrefs.getBoolean("priminimai", false);
        if(priminimai) {
            atsauktiPriminimus();
        } else {
            nustatytiPriminimus();
        }
    }

    /**Funkcija, pritaikanti priminimų nustatymo tekstą padarytam pasirinkimui*/
    private void setPriminimaiTextProperties(boolean priminimai) {
        TextView priminimaiInfo = findViewById(R.id.priminimai_info);
        LinearLayout papildomiNustatymai = findViewById(R.id.papildomiNustatymai);

        if(priminimai) {
            papildomiNustatymai.setVisibility(View.VISIBLE);
            priminimaiInfo.setVisibility(View.GONE);
        } else {
            papildomiNustatymai.setVisibility(View.GONE);
            priminimaiInfo.setVisibility(View.VISIBLE);
            priminimaiInfo.setText(getString(R.string.isjungus_pranesimus));
        }

        changeSettingColor(priminimai, R.id.priminimuBusena);
    }

    /**Funkcija nustatyti visus priminimus apie pamokas paspaudus mygtuk1*/
    public void nustatytiPriminimus() {
        if(Funkcijos.nustatytiPriminimus(getSharedPreferences("label", 0), getApplicationContext()) == 1) {
            Toast.makeText(getApplicationContext(), R.string.nepavyko_atnaujinti_priminimu, Toast.LENGTH_LONG).show();
            finish();
        } else {
            setPriminimaiTextProperties(true);
        }
    }

    /**Funkcija ištrinti visus priminimus apie pamokas paspaudus mygtuką*/
    public void atsauktiPriminimus() {
        Funkcijos.atsauktiPriminimus(getApplicationContext(), mPrefs);
        setPriminimaiTextProperties(false);

        Funkcijos.removeNotification(this, 3);
    }
    /*-------------------------------------------------------------------*/


    /*--------------------------- Vibracija ----------------------------*/
    /**Funkcija, įjungianti ar išjungianti vibraciją*/
    public void pakeistiVibracijosBusena(View view) {
        keite_nustatymus = true;
        vibracija = !mPrefs.getBoolean("vibracija", true);
        mPrefs.edit().putBoolean("vibracija", vibracija).apply();
        changeSettingColor(vibracija, R.id.vibracijosBusena);
    }
    /*-------------------------------------------------------------------*/


    /*--------------------------- Informacija ----------------------------*/
    /**Funkcija, pakeičianti, kokią informaciją rodyti pranešimuose*/
    public void pakeistiInformacijosBusena(View view) {
        keite_nustatymus = true;
        int busena = mPrefs.getInt("informacija", 0);
        switch(busena) {
            case 0:
                busena = 1;
                break;
            case 1:
                busena = 0;
                break;
        }
        findViewById(R.id.informacijos_paaiskinimas).setVisibility(View.VISIBLE);
        mPrefs.edit().putInt("informacija", busena).apply();
        setInformacijaTextProperties(busena);
    }

    /**Funkcija, pritaikanti rodomos informacijos nustatymo tekstą padarytam pasirinkimui*/
    void setInformacijaTextProperties(int busena) {
        TextView informacijosBusena = findViewById(R.id.informacijosBusena);
        switch(busena) {
            case 0:
                informacijosBusena.setText(R.string.smulki_informacija);
                break;
            case 1:
                informacijosBusena.setText(R.string.pabaigos_laikas);
                break;
        }
    }
    /*-------------------------------------------------------------------*/


    /*--------------------------- Panaikinimas ----------------------------*/
    /**Funkcija, įjungianti ar išjungianti pranešimų panaikinimą nubraukiant*/
    public void pakeistiPanaikinimoNustatymus(View view) {
        keite_nustatymus = true;
        panaikinimai = !mPrefs.getBoolean("panaikinimai", false);
        mPrefs.edit().putBoolean("panaikinimai", panaikinimai).apply();
        setPanaikinimaiTextProperties();
    }

    /**Funkcija, pritaikanti pranešimų panaikinimo nustatymo tekstą padarytam pasirinkimui*/
    void setPanaikinimaiTextProperties() {
        TextView panaikinimoInfo = findViewById(R.id.panaikinimo_info);
        panaikinimoInfo.setText(panaikinimai ? getString(R.string.panaikinimai_ijungti) : getString(R.string.panaikinimai_isjungti));
        changeSettingColor(panaikinimai, R.id.panaikinimoBusena);
    }
    /*-------------------------------------------------------------------*/


    /*--------------------------- Nukirpimas ----------------------------*/
    /**Funkcija, pakeičianti pirmo žodžio nuo pavadinimo nukirpimo nustatymus*/
    public void pakeistiNukirpimoNustatymus(View view) {
        keite_nustatymus = true;
        boolean nukirpimas = !mPrefs.getBoolean("nukirpimas", false);
        mPrefs.edit().putBoolean("nukirpimas", nukirpimas).apply();
        findViewById(R.id.trimWarning).setVisibility(View.VISIBLE);
        setNukirpimoTextProperties();
    }

    /**Funkcija, pritaikanti pavadinimo apkirpimo tekstą padarytam pasirinkimui*/
    void setNukirpimoTextProperties() {
        TextView trimInfo = findViewById(R.id.trimInfo);
        boolean nukirpimas = mPrefs.getBoolean("nukirpimas", false);
        changeSettingColor(nukirpimas, R.id.trimBusena);
        trimInfo.setText(nukirpimas ? getString(R.string.nukirpimas_on) : getString(R.string.nukirpimas_isjungtas));
    }
    /*-------------------------------------------------------------------*/


    /*--------------------------- AutoUpdate ----------------------------*/
    public void pakeistiAutoUpdateNustatymus(View view) {
        keite_nustatymus = true;
        boolean autoUpdate = !mPrefs.getBoolean("autoUpdate", true);
        mPrefs.edit().putBoolean("autoUpdate", autoUpdate).apply();
        changeSettingColor(autoUpdate, R.id.autoUpdateBusena);
    }
    /*-------------------------------------------------------------------*/

    /*--------------------------- AutoUpdate ----------------------------*/
    public void pakeistiVersionUpdateNustatymus(View view) {
        keite_nustatymus = true;
        boolean versionUpdate = !mPrefs.getBoolean("versionUpdate", true);
        mPrefs.edit().putBoolean("versionUpdate", versionUpdate).apply();
        changeSettingColor(versionUpdate, R.id.versionUpdateBusena);
    }
    /*-------------------------------------------------------------------*/
    /*--------------------------------------------------------------------------------------*/




    /*--------------------------------Sukurti mokinių sąrašus --------------------------------*/

    /**funkcija sukurti vardu sarasui, jo nustatymams ir elemento (vardo) paspaudimui**/
    void nustatytiSarasa() {
        RecyclerView recyclerView;

        recyclerView = findViewById(R.id.nameList);
        mAdapter = new MokiniaiAdapter(rodomiMokiniai);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        final Mokinys mok = new Mokinys(getString(R.string.press_load_names), "sample");
        rodomiMokiniai.add(mok);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                nustatytiLink(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if(position < 0 || position > mokiniai.size())
                    return;
                if(mokiniai.get(position).getPazymetas()) {
                    String vardas = rodomiMokiniai.get(position).getVardas();
                    for(int i = 0; i < mokiniai.size(); i++)
                        if(mokiniai.get(i).getVardas().equals(vardas) && mokiniai.get(i).getPazymetas())
                            mokiniai.remove(i);
                    Toast.makeText(getApplicationContext(), getString(R.string.atzymetas_mokinys) + rodomiMokiniai.get(position).getVardas(), Toast.LENGTH_SHORT).show();
                } else {
                    mokiniai.add(0, new Mokinys(rodomiMokiniai.get(position)));
                    mokiniai.get(0).setPazymetas(true);
                    Toast.makeText(getApplicationContext(), getString(R.string.pazymetas_mokinys) + rodomiMokiniai.get(position).getVardas(), Toast.LENGTH_SHORT).show();
                }
                atnaujintiRodomusMokinius(inputName.getText().toString());
            }
        }));
    }
    /*----------------------------------------------------------------------------------------*/

    void atnaujintiRodomusMokinius(String s) {
        rodomiMokiniai.clear();
        if(s.equals(""))
            rodomiMokiniai.addAll(mokiniai);
        else
            for(Mokinys m : mokiniai)
                if(m.getVardas().toLowerCase().contains(s.toLowerCase()))
                    rodomiMokiniai.add(m);
        mAdapter.notifyDataSetChanged();
    }


    /*----------------------- Veiksmai su rodomais mokiniais-----------------------------------*/
    /**Funkcija pakeisti rodomiems vardams, kai vartotojas iveda teksta i teksto laukeli*/
    public TextWatcher filterTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            atnaujintiRodomusMokinius(charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) { }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
    };

    /**funkcija pakeisti tvarkarascio nuorodai i pasirinkta is saraso**/
    void nustatytiLink(int position) {
        String link = rodomiMokiniai.get(position).getNuoroda();
        String vardas = rodomiMokiniai.get(position).getVardas();

        if(!link.equals("sample")) {
            keite_nustatymus = true;
            mPrefs.edit().putString("link", link).apply();
            mPrefs.edit().putString("pamokuLink", link).apply();
            mPrefs.edit().putString("pamokosAtnaujintos", Funkcijos.getLaikas()).apply();

            Funkcijos.gautiInformacija(getApplicationContext(), mPrefs, "pamokos"); //is anksto uzkrauname tvarkarasti
            Toast.makeText(getApplicationContext(), getString(R.string.set_link_for) + vardas, Toast.LENGTH_LONG).show();
            Funkcijos.atsauktiPriminimus(getApplicationContext(), mPrefs);
        }
    }
    /*----------------------------------------------------------------------------------------*/




    /*----------------------- Siųstis, analizuoti ir rodyti vardus ----------------------------*/
    /**funkcija tikrinti, ar reikia atnaujinti vardu sarasa**/
    public void parseNames(View view) {
        String nameString = mPrefs.getString("nameString", "NULL");
        if(nameString.equals("NULL")) { //jeigu nebuvo issaugotas vardu sarasas
            updateNameString();
        } else { //jeigu vardu sarasas jau buvo issaugotas
            updateNameListDialog();
        }
    }

    /**Dialogas paklausti, ar vartotojas nori atnaujinti vardu sarasa, ar naudoti sena**/
    void updateNameListDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);

        final TextView dialog_text = dialog.findViewById(R.id.dialog_text),
                positive_button = dialog.findViewById(R.id.dialog_positive_button),
                negative_button = dialog.findViewById(R.id.dialog_negative_button),
                neutral_button = dialog.findViewById(R.id.dialog_neutral_button);

        String text = getString(R.string.list_was_updated) + mPrefs.getString("namesUpdatedAt", "NULL") + getString(R.string.list_was_updated_2_dalis);

        dialog_text.setText(text);
        positive_button.setText(getString(R.string.download_new_list));
        negative_button.setText(getString(R.string.use_old_list));

        positive_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keite_nustatymus = true;
                updateNameString();
                dialog.dismiss();
            }
        });
        negative_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keite_nustatymus = true;
                analyzeNames(mPrefs.getString("nameString", "NULL"));
                dialog.dismiss();
            }
        });
        neutral_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**funkcija atnaujinti vardu sarasui**/
    void updateNameString() {
        Funkcijos.gautiInformacija(getApplicationContext(), mPrefs, "nameString");

        BroadcastReceiver message = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                analyzeNames(mPrefs.getString("nameString", "NULL"));
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(message, new IntentFilter("name_download_finished"));

        mPrefs.edit().putString("namesUpdatedAt", Funkcijos.getLaikas()).apply();
    }

    /**funkcija vardu saraso HTML tekstui suskirstyti i kintamuosius*/
    public void analyzeNames(String result) {
        inputName.setHint(R.string.iveskite_varda);
        String [] resultArr = result.split("\n", 0); //skaidome sakini visur, kur prasideda nauja eilute
        boolean moksleiviai = false; //ar siuo metu skaitomas sakinys priklauso "Moksleiviai" lenteles skilciai
        mokiniai.clear(); //panaikiname standartinius elementus sarase
        rodomiMokiniai.clear();

        for(String a : resultArr) {
            String nameInString = "", linkInString = "";
            if(moksleiviai && a.contains("<a h")) {
                nameInString = Funkcijos.findNameInString(a, false); //pazymime, kad sakinyje nurodoma mokinio name bei pavarde
                linkInString = Funkcijos.findLinkInString(getApplicationContext(), a);
            } else if(a.contains("Klasės"))
                moksleiviai = true;

            if(!nameInString.equals("")) {
                Mokinys mok = new Mokinys(nameInString, linkInString);
                mokiniai.add(mok); //pridedame nuskaitytus mokinio duomenis i sarasa
                rodomiMokiniai.add(mok);
            }
        }

        if(mokiniai.size() > 0) {
            mokiniai.remove(mokiniai.size() - 1); //pasaliname paskutini - "mimosasoftware.com" elementa
            rodomiMokiniai.remove(rodomiMokiniai.size() - 1);
        }
        mAdapter.notifyDataSetChanged();

        issaugotiMokinius(mokiniai);
    }



    private void issaugotiMokinius(List <Mokinys> mokiniai) {
        Funkcijos.setList(mPrefs, "mokiniuSarasas", mokiniai);
        mPrefs.edit().putString("issaugotoSarasoTekstas", mPrefs.getString("nameString", "ERROR")).apply();
        mPrefs.edit().putBoolean("issaugotoSarasoSwitched", mPrefs.getBoolean("switched", false)).apply();
    }
    /*----------------------------------------------------------------------------------------*/
}
