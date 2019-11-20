package com.example.user.myapplication;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     *      MainActivity naudojami SharedPreferences kintamieji:
     * pamokos              asmeninio tvarkarascio puslapio HTML tekstas
     * pamokosAtnaujintos   laikas, kada paskutini karta atnaujintas pamoku HTML tekstas
     * pamokuLink           nuoroda, is kurios paskutini karta siustas pamoku HTML tekstas
     * link                 mokinio asmeninio tvarkarascio nuoroda
     * */

    List<Pamoka> pamokos = new ArrayList<>();
    public PamokosAdapter pamokosAdapter;
    RecyclerView.LayoutManager pamokosLayoutManager;
    RecyclerView dienuRecyclerView;
    RecyclerView pamokuRecyclerView;

    List<String> dienos = new ArrayList<>();
    DienosAdapter dienosAdapter;

    Handler mHandler, resumeHandler;

    SharedPreferences mPrefs;
    int pasirinktaDiena;
    Tvarkarastis tvarkarastis;
    int [][] pamokuLaikas = new int[18][2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPrefs = getSharedPreferences("label", 0);
        if(mPrefs.getBoolean("darkTheme", false))
            setTheme(R.style.DarkTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(mPrefs.getBoolean("darkTheme",false)) {
            ImageView settingsButton = findViewById(R.id.settingsButton);
            settingsButton.setImageDrawable(getDrawable(R.drawable.settings_white));
        }

        pasirinktaDiena = Funkcijos.parinktiDiena(mPrefs, true);
        mPrefs.edit().putInt("pasirinktaDiena", pasirinktaDiena).apply();

        tvarkarastis = new Tvarkarastis();

        createHandler(); //paruošiame dienų, pamokų sąrašus ir į juos įrąšome informaciją
        createResumeHandler();

        PrepareLists runner = new PrepareLists();
        runner.execute();

        if(mPrefs.getBoolean("autoUpdate", true))
            Funkcijos.nustatytiPamokuAtnaujinima(getApplicationContext(), mPrefs);

        mPrefs.edit().putBoolean("mainActivityDarkTheme", mPrefs.getBoolean("darkTheme", false)).apply();
        checkForMessages();
        checkForUpdates();

        BroadcastReceiver message = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                paruostiSarasa();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(message, new IntentFilter("lesson_download_finished"));
    }

    void displayUpdateDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_one_button_dialog);

        final TextView dialog_text = dialog.findViewById(R.id.dialog_text),
                button = dialog.findViewById(R.id.dialog_button);

        String text = getString(R.string.nauja_versija);

        dialog_text.setText(text);

        dialog_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tinklalapioIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/gvgtvarkarastis"));
                startActivity(tinklalapioIntent);
            }
        });

        button.setText(R.string.gerai);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    void checkForUpdates() {
        Funkcijos.getString(getApplicationContext(), "https://eidenisk.github.io/tvarkarastis/version.html", mPrefs, "web-version");
        BroadcastReceiver message = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String version = mPrefs.getString("web-version", "NULL");
                if(version.indexOf('/') != -1)
                    version = version.substring(version.indexOf('v'), version.indexOf('/'));
                else
                    Log.d("myDebug","problem with version codes, wait and reload or contact the developer");
                if(!version.equals(BuildConfig.VERSION_NAME) && mPrefs.getBoolean("versionUpdate", true)) {
                    displayUpdateDialog();
                }
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(message, new IntentFilter("version_check_finished"));
    }

    void displayInfoDialog(String info) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_one_button_dialog);

        final TextView dialog_text = dialog.findViewById(R.id.dialog_text),
                button = dialog.findViewById(R.id.dialog_button);

        dialog_text.setText(info);

        dialog_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tinklalapioIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/gvgtvarkarastis"));
                startActivity(tinklalapioIntent);
            }
        });

        button.setText(R.string.gerai);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    void checkForMessages() {
        Funkcijos.getString(getApplicationContext(), "https://eidenisk.github.io/tvarkarastis/info.html", mPrefs, "web-info");
        BroadcastReceiver message = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    String info_whole = mPrefs.getString("web-info", "NULL");
                    String info = Jsoup.parse(info_whole).select("a[id=\"info\"]").get(0).text();
                    displayInfoDialog(info);
                } catch (Exception e) {
                    Log.d("myDebug", "problem with web information, wait and reload or contact the developer");
                }

            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(message, new IntentFilter("info_check_finished"));
    }

    /*--------------- Paruošti dienų ir pamokų sąrašus pirmą kartą atidarius programą -------------------*/
    /**Klasė, sukurianti pagrindinio ekrano sąrašus ir tik tada juos atnaujinanti*/
    private class PrepareLists extends AsyncTask <Void, Void, Void> {
        public PrepareLists() {}

        @Override
        protected Void doInBackground(Void... params) { //sukuriame:...
            nustatytiSarasa(); //pamokų sąrašą
            nustatytiDienuSarasa(); //dienų sąrašą
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            paruostiSarasa(); //į sukurtus sąrašus įkeliame reikiamą informaciją
        }
    }

    /**Funkcija sukurti pamoku sarasui, jo nustatymams*/
    void nustatytiSarasa() {
        pamokuRecyclerView = findViewById(R.id.recView);
        pamokosAdapter = new PamokosAdapter(pamokos, mPrefs, getApplicationContext());

        pamokosLayoutManager = new LinearLayoutManager(getApplicationContext());
        pamokuRecyclerView.setLayoutManager(pamokosLayoutManager);
        pamokuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        pamokuRecyclerView.setAdapter(pamokosAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(pamokuRecyclerView.getContext(), LinearLayoutManager.VERTICAL);
        pamokuRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    /**Funkcija sukurti dienu sarasui, jo nustatymams*/
    void nustatytiDienuSarasa() {
        dienuRecyclerView = findViewById(R.id.dienuList);
        dienosAdapter = new DienosAdapter(dienos, mPrefs, getApplicationContext());
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());

        dienuRecyclerView.setLayoutManager(mLayoutManager);
        dienuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        dienuRecyclerView.setAdapter(dienosAdapter);

        String[] dienuStr = {"Pr", "An", "Tr", "Kt", "Pn"};
        dienos.addAll(Arrays.asList(dienuStr));
        dienosAdapter.notifyDataSetChanged();

        Message msg = mHandler.obtainMessage();
        msg.sendToTarget();
    }
    /*----------------------------------------------------------------------------------------------------*/




    /*------------------------ Atnaujinti, siųsti pamokas, rodyti dialogą --------------------------------*/
    /**Funkcija tikrinti, ar reikia atnaujinti pamoku sarasa*/
    public void parseLessons(View view) {
        String link = mPrefs.getString("link", "NULL_LINK");
        String pamokuLink = mPrefs.getString("pamokuLink", "NULL");

        if(link.equals("NULL_LINK")) {
            Toast.makeText(getApplicationContext(), R.string.pasirinkite_mokini, Toast.LENGTH_SHORT).show();
            return;
        }

        //jeigu sutampa jau turimu pamoku ir norimo tvarkarascio nuoroda, klausiame, ar siusti is naujo
        if(link.equals(pamokuLink)) updateNameListDialog();
        else updatePamokuString(); //jeigu nesutampa, parsiuncame nauja tvarkarasti
    }

    /**Dialogas paklausti, ar vartotojas nori atnaujinti pamoku sarasa, ar naudoti sena*/
    void updateNameListDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);

        final TextView dialog_text = dialog.findViewById(R.id.dialog_text),
                positive_button = dialog.findViewById(R.id.dialog_positive_button),
                negative_button = dialog.findViewById(R.id.dialog_negative_button),
                neutral_button = dialog.findViewById(R.id.dialog_neutral_button);

        String text = getString(R.string.lessons_were_1_dalis) + mPrefs.getString("pamokosAtnaujintos", "NULL") + getString(R.string.lessons_were_2_dalis);

        dialog_text.setText(text);
        positive_button.setText(getString(R.string.download_new_lessons));
        negative_button.setText(getString(R.string.use_old_lessons));

        positive_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePamokuString(); //jeigu pasirinko vis tiek atnaujinti pamoku sarasa
                dialog.dismiss();
            }
        });
        negative_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paruostiSarasa();
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

    /**Funkcija atnaujinti pamoku sarasui*/
    void updatePamokuString() {
        Funkcijos.gautiInformacija(this, mPrefs, "pamokos");
        BroadcastReceiver message = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPrefs.edit().putString("pamokuLink", mPrefs.getString("link", "NULL")).apply(); //nuoroda, is kurios siunteme
                mPrefs.edit().putString("pamokosAtnaujintos", Funkcijos.getLaikas()).apply(); //laikas, kada siunteme pamokas
                paruostiSarasa();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(message, new IntentFilter("lesson_download_finished"));
    }

    /**Funkcija sukurti ir paleisti ReloadLessons AsyncTask subklasę*/
    void paruostiSarasa() {
        ReloadLessons runner = new ReloadLessons();
        runner.execute();
    }

    /**Funkcija gautus kintamuosius is pamoku saraso pritaikyti naudojimui sarase, ji atnaujinti*/
    private class ReloadLessons extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String pamokos = mPrefs.getString("pamokos", "NULL"); //išanalizuojame pamokų HTML tekstą
            if (!pamokos.equals("NULL")) {
                Funkcijos.analyzeString(pamokos, tvarkarastis, getSharedPreferences("label", 0), getApplicationContext());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(!tvarkarastis.klase.equals("")) {
                TextView infoView = findViewById(R.id.infoView);
                infoView.setText(tvarkarastis.pavadinimas);
                TextView classView = findViewById(R.id.classView);
                classView.setText(tvarkarastis.klase.trim());

                if(tvarkarastis.pamokos[0][0].getLaikas().equals("")) {//kiekviena karta nereikia is naujo pildyti laiko
                    Funkcijos.getPamokuLaikas(tvarkarastis, pamokuLaikas);
                    for (int pamID = 0; pamID < tvarkarastis.maxPamoku; pamID++) {
                        for (int diena = 0; diena < 5; diena++)
                            tvarkarastis.pamokos[diena][pamID].setLaikas(tvarkarastis.laikas[pamID]);
                    }
                }
                pakeistiPasirinktasPamokas();
                pamokosAdapter.notifyDataSetChanged();
            }
        }
    }
    /*---------------------------------------------------------------------------------------------------*/




    /*------- Ne pirmą kartą atidarius programą pakeisti pažymėtą pamoką --------------------------------*/
    void createResumeHandler() {
        resumeHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                pakeistiPasirinktasPamokas();
            }
        };
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        pasirinktaDiena = Funkcijos.parinktiDiena(mPrefs, true);
        mPrefs.edit().putInt("pasirinktaDiena", pasirinktaDiena).apply();
        Message msg = resumeHandler.obtainMessage();
        msg.sendToTarget();
    }
    /*---------------------------------------------------------------------------------------------------*/




    /*---------- Pakeisti rodomas pamokas ir nuspalvinti dabartinės dienos bei pamokos langelius --------*/
    /**Dienų pasirinkimas paspaudus*/
    void createHandler() {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                dienuRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), dienuRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        pasirinktaDiena = position;
                        mPrefs.edit().putInt("pasirinktaDiena", pasirinktaDiena).apply();
                        pakeistiPasirinktasPamokas();
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {}
                }));
            }
        };
    }

    void pakeistiPasirinktasPamokas() {
        updateDayColor();
        updateSelectedLessons();
    }

    private void updateDayColor() {
        //PAZYMETI PASIRINKTA DIENA (PAKEISTI FONO SPALVA)
        for(int x = 0; x < dienuRecyclerView.getChildCount(); x++) {
            int col;
            if(x == pasirinktaDiena) col = (!mPrefs.getBoolean("darkTheme", false)) ? ContextCompat.getColor(getApplicationContext(), R.color.selectedItemColor) : ContextCompat.getColor(getApplicationContext(), R.color.selectedItemColor1);
            else col = (!mPrefs.getBoolean("darkTheme", false)) ?  ContextCompat.getColor(getApplicationContext(), R.color.defaultItemColor) : ContextCompat.getColor(getApplicationContext(), R.color.defaultItemColor1);
            dienuRecyclerView.getChildAt(x).setBackgroundColor(col);
        }
        dienosAdapter.notifyDataSetChanged();
    }

    private void updateSelectedLessons() {
        //RODYTI TIK TOS DIENOS PAMOKAS
        pamokos.clear(); //panaikiname pries tai rodytas pamokas
        pamokos.addAll(Arrays.asList(tvarkarastis.pamokos[pasirinktaDiena]));

        while (pamokos.size() > 0 && pamokos.get(pamokos.size() - 1).getPavadinimas().equals(""))
            pamokos.remove(pamokos.size() - 1);
        if(pamokos.size() == 0)
            Toast.makeText(getApplicationContext(), R.string.pasirinkite_mokini, Toast.LENGTH_SHORT).show();

        pamokosLayoutManager.scrollToPosition(0);
        pamokosAdapter.notifyDataSetChanged();
    }
    /*---------------------------------------------------------------------------------------------------*/




    public void openSettings(View view) {
        Intent intent = new Intent(this, SelectSettingsActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode == RESULT_OK) {
            if(data.getBooleanExtra("keite_nustatymus", false)) {
                int col = (!mPrefs.getBoolean("darkTheme", false)) ? ContextCompat.getColor(getApplicationContext(), R.color.selectedItemColor) : ContextCompat.getColor(getApplicationContext(), R.color.selectedItemColor1);
                dienuRecyclerView.getChildAt(pasirinktaDiena).setBackgroundColor(col);
                paruostiSarasa();
                if (mPrefs.getBoolean("priminimai", false))
                    Funkcijos.pakeistiPriminimus(mPrefs, getApplicationContext());
            }
            if(data.getBooleanExtra("needs_restart", false))
                recreate();
        }
    }
}
