package com.example.user.myapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Funkcijos {

    /*-----------------------------Laiko ir datos funkcijos--------------------------------------*/
    /**Funkcija gauti dabartiniam laikui
     * PASTABA: formatas - "MM-DD MM:SS"*/
    static String getLaikas() {
        Calendar c = Calendar.getInstance();
        String [] laikas = new String[4];
        laikas[0] = Integer.toString(c.get(Calendar.MONTH)+1);
        laikas[1] = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
        laikas[2] = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
        laikas[3] = Integer.toString(c.get(Calendar.MINUTE));
        for(int x = 0; x < 4; x++)
            if(laikas[x].length() == 1) laikas[x] = "0" + laikas[x];
        return(laikas[0] + "-" + laikas[1] + " " + laikas[2] + ":" + laikas[3]);
    }

    /**Funkcija gauti dabartinei savaites dienai
     * PASTABA: Pr = 0 ... Pn = 4 Ses = 5 Sek = 6*/
    private static int getDiena() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DAY_OF_WEEK);
    }

    /**Jeigu dabar darbo diena, nustato ja kaip pasirinkta diena, kitu atveju pasirenka pirmadieni*/
    public static int parinktiDiena(SharedPreferences mPrefs, boolean rollOver) {
        int d = Funkcijos.getDiena()-2;
        Calendar currentTime = Calendar.getInstance();
        Calendar pamokuPabaiga = Calendar.getInstance();
        pamokuPabaiga.set(Calendar.HOUR_OF_DAY, mPrefs.getInt("rollOverH" + Integer.toString(d), 15));
        pamokuPabaiga.set(Calendar.MINUTE, mPrefs.getInt("rollOverM" + Integer.toString(d), 50));
        pamokuPabaiga.set(Calendar.SECOND, 0);

        if(d < 5 && d >= 0)
            return (rollOver && currentTime.after(pamokuPabaiga)) ? (d != 4 ? (d + 1) : 0) : d;
        else
            return 0;
    }

    private static void atnaujintiPabaigosLaika(Tvarkarastis tvarkarastis, SharedPreferences mPrefs) {
        int [][] pamokuLaikas = new int[18][2];
        getPamokuLaikas(tvarkarastis, pamokuLaikas);

        for(int diena = 0; diena < 5; diena++) {
            int last_id = 0;
            for(int pamoka = 0; pamoka < tvarkarastis.maxPamoku; pamoka++)
                if(!tvarkarastis.pamokos[diena][pamoka].getPavadinimas().equals(""))
                    last_id = pamoka;

            if(last_id != 0) {
                mPrefs.edit().putInt("rollOverH" + Integer.toString(diena), pamokuLaikas[last_id*2+1][0]).apply();
                mPrefs.edit().putInt("rollOverM" + Integer.toString(diena), pamokuLaikas[last_id*2+1][1]).apply();
            }
        }
    }
    /*-------------------------------------------------------------------------------------------------------*/




    /*----------------------------- Pamokų analizavimo funkcijos --------------------------------------*/
    /**Funkcija pamoku saraso HTML tekstui skaidyti i kintamuosius*/
    public static void analyzeString(String result, Tvarkarastis tvarkarastis, SharedPreferences mPrefs, Context context) {
        tvarkarastis.clear();
        Document doc = Jsoup.parse(result);
        try {
            tvarkarastis.pavadinimas = doc.select("font[size=5]").get(0).text();
            int end_of_1 = tvarkarastis.pavadinimas.indexOf("gimnazija") + 9;
            int start_of_2 = end_of_1;
            while( !Character.isLetterOrDigit( tvarkarastis.pavadinimas.charAt(start_of_2) ) )
                start_of_2++;

            tvarkarastis.pavadinimas = tvarkarastis.pavadinimas.substring(0, end_of_1) + "\n" + tvarkarastis.pavadinimas.substring(start_of_2);
        } catch(Exception e) {
            Log.d("myDebug", "ERROR");
        }

        Element table = doc.select("table").get(0);
        Elements rows = table.select("tr");
        tvarkarastis.klase = rows.get(0).select("td").get(0).text();

        for(int i = 2; i < rows.size(); i++) {
            Elements cells = rows.get(i).select("td");
            tvarkarastis.laikas[i-2] = cells.get(0).text();

            for(int j = 1; j < cells.size(); j++) {
                tvarkarastis.pamokos[j-1][i-2] = new Pamoka();
                tvarkarastis.pamokos[j-1][i-2].setLaikas(tvarkarastis.laikas[i-2]);
                tvarkarastis.pamokos[j-1][i-2].setNumeris(i-1);

                String allText = cells.get(j).text();
                String tempStr = cells.get(j).select("a").text();
                if(tempStr.equals("/////") || tempStr.equals("           ")) continue;
                //tvarkarastis.pamokos[j-1][i-2].addMokytojai(allText.replaceAll(tempStr, "").trim().replaceAll("\\u00A0", ""));

                if(mPrefs.getBoolean("nukirpimas", false)) {
                    String [] words = tempStr.split(" ");
                    words[0] = "";
                    for(int idx = 0; idx < words.length; idx++) {
                        if(words[idx].toLowerCase().equals("srautas".toLowerCase())) {
                            words[idx] = "";
                            words[idx -1] = "";
                        } else if(words[idx].equals("A") || words[idx].equals("B"))
                            words[idx] = "";
                    }
                    String final_string = "";
                    for(int idx = 0; idx < words.length; idx++)
                        if(!words[idx].equals(""))
                            final_string += (idx != 0 && final_string.length() != 0 ? " " : "") + words[idx];
                    tvarkarastis.pamokos[j-1][i-2].setPavadinimas(final_string);
                } else
                    tvarkarastis.pamokos[j-1][i-2].setPavadinimas(tempStr);


                String nuoroda_pamokai = mPrefs.getString("main_link", "null") + cells.get(j).select("a").attr("href");
                //Log.d("myDebug", nuoroda_pamokai);
                tvarkarastis.pamokos[j-1][i-2].nuoroda(nuoroda_pamokai);
            }
        }

        tvarkarastis.intLaikoSuma = Funkcijos.getPamokuLaikas(tvarkarastis, tvarkarastis.intLaikas);

        Gson gson = new Gson();
        String tvarkarascio_string = gson.toJson(tvarkarastis);
        mPrefs.edit().putString("tvarkarastis", tvarkarascio_string).apply();

        atnaujintiPabaigosLaika(tvarkarastis, mPrefs);
        updateWidget(context);
    }

    public static void updateWidget(Context context) {
        Intent intent = new Intent(context, MyWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context.getApplicationContext(), MyWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);

        intent = new Intent(context, MyWidgetProviderLight.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context.getApplicationContext(), MyWidgetProviderLight.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    public static String findNameInString(String a, boolean autoComplete) {
        int tagStart = a.indexOf('<'),
                tagEnd = a.indexOf('>');

        while(tagStart - tagEnd < 3) {
            if(tagStart < tagEnd)
                a = a.replaceFirst("<", "");
            else
                a = a.replaceFirst(">", "");
            tagStart = a.indexOf('<');
            tagEnd = a.indexOf('>');

            if(tagStart == -1 || tagEnd == -1)
                break;
        }
        if(tagStart != -1 && tagEnd != -1)
            return a.substring(tagEnd+1, tagStart);
        else return "";
    }

    public static String findLinkInString(Context context, String a) {
        int propertyStart = -1, propertyEnd = -1;

        while(a.indexOf('"') != -1) {
            propertyStart = a.indexOf('"');
            a = a.replaceFirst("\"", "");
            propertyEnd = a.indexOf('"');
            a = a.replaceFirst("\"", "");
        }

        if(propertyStart != -1 && propertyEnd != -1) {
            String link = context.getSharedPreferences("label", 0).getString("main_link", "NULL");
            if(!link.equals("NULL")) {
                if (link.charAt(link.length() - 1) != '/')
                    link += "/";
                return link + a.substring(propertyStart, propertyEnd);
            }
            else return "";
        }
        else return "";
    }
    /*-------------------------------------------------------------------------------------------------------*/




    /*-----------------------------Kintamųjų nuskaitymo/saugojimo funkcijos--------------------------------------*/
    /**Funkcija iš atminties gauti pažymėtų mokinių sąrašui*/
    public static List<Mokinys> getList(SharedPreferences mPrefs, String key) {
        String json = mPrefs.getString(key, "NULL");
        if(json.equals("NULL"))
            return new ArrayList<>();
        else {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Mokinys>>(){}.getType();
            try {
                return gson.fromJson(json, listType);
            } catch(Exception e) {
                return new ArrayList<>();
            }
        }
    }

    /**Funkcija į atmintį įrašyti pažymėtų mokinių sąrašą*/
    public static void setList(SharedPreferences mPrefs, String key, List<Mokinys> list) {
        Gson gson = new Gson();
        mPrefs.edit().putString(key, gson.toJson(list)).apply();
    }

    /**Funkcija, iš tvarkaračšio į integer masyvą nuskaitanti pamokų pradžios ir pabaigos laikus*/
    public static int getPamokuLaikas(Tvarkarastis tvarkarastis, int target[][]) {
        int suma = 0;
        for(int pamID = 0; pamID < tvarkarastis.maxPamoku; pamID++) {
            String temp = tvarkarastis.laikas[pamID];
            Log.d("myDebug", temp);
            String[] laikas = new String[]{temp.substring(0, 2), temp.substring(3, 5), temp.substring(6, 8), temp.substring(9)};
            for (int i = 0; i < 2; i++)
                for (int j = 0; j < 2; j++) {
                    target[pamID * 2 + i][j] = Integer.parseInt(laikas[i * 2 + j]);
                    suma += target[pamID *2 +i][j];
                }
        }
        return suma;
    }

    /**Funkcija, iš atminties nuskaitanti išsaugotą tvarkaraštį*/
    public static Tvarkarastis getTvarkarastis(SharedPreferences mPrefs) {
        Tvarkarastis tvarkarastis;
        String json = mPrefs.getString("tvarkarastis", "NULL");
        if(json.equals("NULL")) {
            tvarkarastis = new Tvarkarastis("NULL");
        } else {
            Gson gson = new Gson();
            Type tvarkarascioType = new TypeToken<Tvarkarastis>(){}.getType();
            try {
                tvarkarastis = gson.fromJson(json, tvarkarascioType);
            } catch (Exception e) {
                Log.d("myDebug", "KLAIDA kraunant tvarkaraštį iš atminties");
                tvarkarastis = new Tvarkarastis("NULL");
            }
        }
        return tvarkarastis;
    }

    /**Bendrine funkcija HTML tekstui parsiusti is svetaines
     * PASTABA: naudojama Windows-1257 koduote
     * @param link tinklalapio nuoroda, is kurios siusti teksta
     * @param target kintamojo SharedPreferences rinkinyje pavadinimas, i kuri irasyti parsiusta teksta*/
    public static void getString(final Context context, final String link, final SharedPreferences mPrefs, final String target) {
        Ion.with(context)
                .load(link)
                .asByteArray()
                .setCallback(new FutureCallback<byte[]>() {
                    @Override
                    public void onCompleted(Exception e, byte[] result) {
                        try {
                            String res;
                            if(target.equals("main_link"))
                                res = new String(result, "UTF-8");
                            else res = new String(result, "Windows-1257");
                            mPrefs.edit().putString(target, res).apply();
                            Intent intent = null;
                            switch(target) {
                                case "nameString": intent = new Intent("name_download_finished"); break;
                                case "pamokos": intent = new Intent("lesson_download_finished"); break;
                                case "web-version": intent = new Intent("version_check_finished"); break;
                                case "main_link": updateMainLink(context, mPrefs, res); break;
                            }
                            if(intent != null)
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        } catch (Exception ex) {
                            Toast.makeText(context, R.string.nepavyko_atsisiusti, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public static void gautiInformacija(Context context, final SharedPreferences mPrefs, final String target) {
        getString(context, context.getString(R.string.main_link), mPrefs, "main_link");
        BroadcastReceiver message = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(target.equals("pamokos"))
                    getString(context, mPrefs.getString("link", "NULL"), mPrefs, target);
                else if(target.equals("nameString"))
                    getString(context, mPrefs.getString("main_link", "NULL"), mPrefs, target);
                else
                    Toast.makeText(context, R.string.klaida_main_link2, Toast.LENGTH_SHORT).show();
            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(message, new IntentFilter("link_download_finished"));
    }
    /*----------------------------------------------------------------------------------------*/

    static private void updateMainLink(Context context, SharedPreferences mPrefs, String res) {
        int idx = res.indexOf(">Tvarkaraštis<") -1;
        if(idx != -1) {
            while(!res.substring(idx, idx +4).equals("href"))
                idx--;
            String link = res.substring(idx+6, res.indexOf('"', idx+6));
            mPrefs.edit().putString("main_link", link).apply();
            Intent intent = new Intent("link_download_finished");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } else Toast.makeText(context, R.string.klaida_main_link1, Toast.LENGTH_SHORT).show();
    }


    /*----------------------------- Priminimų funkcijos -----------------------------------------*/
    /**Funkcija, suplanuojanti tos dienos priminimus ir jų atnaujinimą*/
    public static int nustatytiPriminimus(SharedPreferences mPrefs, Context context) {
        Tvarkarastis tvarkarastis = Funkcijos.getTvarkarastis(mPrefs); //nuskaitome išsaugotą tvarkaraštį
        if(tvarkarastis.pavadinimas.equals("NULL")) //jeigu tokio nėra, grįžtame atgal
            return 1;

        if(tvarkarastis.intLaikoSuma == 0)
            tvarkarastis.intLaikoSuma = Funkcijos.getPamokuLaikas(tvarkarastis, tvarkarastis.intLaikas);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int pasirinktaDiena = parinktiDiena(mPrefs, false);

        int dabar = Funkcijos.getDiena() -2;
        if(dabar < 5 && dabar >= 0) {
            int lastUnsetTimeID = -1, lastTimeID = -1;
            for (int pamoka = 0; pamoka < tvarkarastis.maxPamoku; pamoka++) { //nustatome kiekvienos pamokos pradžios bei pabaigos priminimus
                if (!tvarkarastis.pamokos[pasirinktaDiena][pamoka].getPavadinimas().equals("")) {
                    if(setNotificationAlarm(context, mPrefs, alarmManager, tvarkarastis, pasirinktaDiena, pamoka * 2, "Dabar ", false) == 1)
                        lastUnsetTimeID = pamoka *2;
                    lastTimeID = pamoka *2;
                }
                if (pamoka < tvarkarastis.maxPamoku - 1 && !tvarkarastis.pamokos[pasirinktaDiena][pamoka + 1].getPavadinimas().equals("")) {
                    if (setNotificationAlarm(context, mPrefs, alarmManager, tvarkarastis, pasirinktaDiena, pamoka * 2 + 1, "Kita ", false) == 1)
                        lastUnsetTimeID = pamoka *2 + 1;
                    lastTimeID = pamoka *2 +1;
                }
            }

            if(lastUnsetTimeID != -1 && lastUnsetTimeID != lastTimeID) { //Parodome paskutinę pamoką, kuriai nenustatytas pranešimas (jeigu ji dar nesibaigė)
                setNotificationAlarm(context, mPrefs, alarmManager, tvarkarastis, pasirinktaDiena, lastUnsetTimeID, ( (lastUnsetTimeID % 2 == 0) ? "Dabar " : "Kita "), true);
            }
        }

        nustatytiKitaiDienai(context, alarmManager, mPrefs);
        if(mPrefs.getBoolean("autoUpdate", true))
            nustatytiPamokuAtnaujinima(context, mPrefs);
        mPrefs.edit().putBoolean("priminimai", true).apply();

        return 0;
    }

    /**Funkcija nustatyti priminimą apie tam tikros pamokos pradžią ar pabaigą*/
    private static int setNotificationAlarm(Context context, SharedPreferences mPrefs, AlarmManager alarmManager, Tvarkarastis tvarkarastis, int pasirinktaDiena, int ID, String prefix, boolean skipCheck) {
        Calendar setCal = Calendar.getInstance();
        Calendar current = Calendar.getInstance();
        if(skipCheck) {
            setCal.set(Calendar.HOUR_OF_DAY, tvarkarastis.intLaikas[ID+1][0]);
            setCal.set(Calendar.MINUTE, tvarkarastis.intLaikas[ID+1][1]);
            setCal.set(Calendar.SECOND, 0);
            if(setCal.before(current))
                return 1;
        } else {
            setCal.set(Calendar.HOUR_OF_DAY, tvarkarastis.intLaikas[ID][0]);
            setCal.set(Calendar.MINUTE, tvarkarastis.intLaikas[ID][1]);
            setCal.set(Calendar.SECOND, 0);
            if(setCal.before(current))
                return 1;
        }

        String title, details, pabaiga;
        if(prefix.equals("Dabar ")) {
            title = tvarkarastis.pamokos[pasirinktaDiena][ID/2].getPavadinimas();
            details = tvarkarastis.pamokos[pasirinktaDiena][ID/2].getMokytojai();
            pabaiga = "Pasibaigs ";
        } else {
            title = tvarkarastis.pamokos[pasirinktaDiena][ID/2+1].getPavadinimas();
            details = tvarkarastis.pamokos[pasirinktaDiena][ID/2+1].getMokytojai();
            pabaiga = "Prasidės ";
        }

        if(tvarkarastis.intLaikas[ID+1][0] < 10) pabaiga += "0";
        pabaiga += tvarkarastis.intLaikas[ID+1][0] + ":";
        if(tvarkarastis.intLaikas[ID+1][1] < 10) pabaiga += "0";
        pabaiga += tvarkarastis.intLaikas[ID+1][1];

        int pabH = tvarkarastis.intLaikas[ID+1][0];
        int pabM = tvarkarastis.intLaikas[ID+1][1];

        if(skipCheck) {
            String info = "ERROR";
            switch (mPrefs.getInt("informacija", 0)) {
                case 0: info = details; break;
                case 1: info = pabaiga; break;
            }
            boolean vibracija = mPrefs.getBoolean("vibracija", true),
                    panaikinimai = mPrefs.getBoolean("panaikinimai", false);
            displayNotification(context, prefix + title, info, vibracija, panaikinimai);
        } else {
            Funkcijos.setAlarm(alarmManager, context, prefix + title, details, pabaiga, pabH, pabM, setCal, ID);
        }

        if(prefix.equals("Dabar ")) {
            setCal.add(Calendar.MINUTE, 45); //Po 45 minučių (pamokos trukmės) pranešimą pašaliname
            nustatytiAtsaukima(context, alarmManager, setCal, ID / 2 +100);
        }
        return 0;
    }

    public static void displayNotification(Context context, String title, String info, boolean vibracija, boolean panaikinimai) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Integer.toString(3))
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_stat_info);

        if(vibracija) builder.setVibrate(new long[]{0, 150, 0, 0, 0});
        if(!panaikinimai) builder.setOngoing(true);

        builder.setContentText(info);

        Intent notifyIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(3, notificationCompat);
    }

    /**Funkcija, nustatanti priminimą nurodytu laiku su nurodyta informacija*/
    private static void setAlarm(AlarmManager alarmManager, Context context, String title, String details, String pabaiga, int pabaigosH, int pabaigosM, Calendar setCal, int ID) {
        Intent notifyIntent = new Intent(context, MyReceiver.class);
        notifyIntent.putExtra("title", title);
        notifyIntent.putExtra("details", details);
        notifyIntent.putExtra("pabaiga", pabaiga);
        notifyIntent.putExtra("pabH", pabaigosH);
        notifyIntent.putExtra("pabM", pabaigosM);
        notifyIntent.putExtra("task", "show");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, setCal.getTimeInMillis(), pendingIntent);
    }

    /**Funkcija, nustatanti pranešimų atnaujinimo priminimą (kitai dienai)*/
    private static void nustatytiKitaiDienai(Context context, AlarmManager alarmManager, SharedPreferences mPrefs) {
        int h = mPrefs.getInt("atnaujintiH", 7);
        int m = mPrefs.getInt("atnaujintiM", 50);

        Calendar updateCal = Calendar.getInstance();
        updateCal.add(Calendar.DATE, 1); //nustatome atnaujinti pamokas KITOS dienos 07:50:00
        updateCal.set(Calendar.HOUR_OF_DAY, h);
        updateCal.set(Calendar.MINUTE, m);
        updateCal.set(Calendar.SECOND, 0);

        int d = updateCal.get(Calendar.DAY_OF_WEEK) -2;

        while(!(d < 5 && d >= 0)) { //toliname datą, kol pasiekiame darbo dieną
            updateCal.add(Calendar.DATE, 1);
            d = updateCal.get(Calendar.DAY_OF_WEEK);
        }

        Intent notifyIntent = new Intent(context, MyReceiver.class);
        notifyIntent.putExtra("task", "update");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 99, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, updateCal.getTimeInMillis(), pendingIntent);
    }

    /**Funkcija, nustatanti pranešimų atšaukimą pamokos pabaigoje*/
    private static void nustatytiAtsaukima(Context context, AlarmManager alarmManager, Calendar setCal, int ID) {
        Intent notifyIntent = new Intent(context, MyReceiver.class);
        notifyIntent.putExtra("task", "remove");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, setCal.getTimeInMillis(), pendingIntent);
    }

    /**Funkcija, atšaukianti VISUS nustatytus priminimus*/
    public static void atsauktiPriminimus(Context context, SharedPreferences mPrefs) {
        Intent notifyIntent = new Intent(context, MyReceiver.class);

        for(int i = 0; i < 110; i++) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }

        mPrefs.edit().putBoolean("priminimai", false).apply();
    }

    /**Funkcija, atšaukianti ir iš naujo nustatanti priminimus*/
    public static void pakeistiPriminimus(SharedPreferences mPrefs, Context context) {
        atsauktiPriminimus(context, mPrefs);
        nustatytiPriminimus(mPrefs, context);
    }

    /**Funkcija, pašalinanti dabar rodomą pranešimą*/
    public static void removeNotification(Context context, int NOTIFICATION_ID) {
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.cancel(NOTIFICATION_ID);
    }

    public static void nustatytiPamokuAtnaujinima(Context context, SharedPreferences mPrefs) {
        String json = mPrefs.getString("autoUpdateTime", "NULL");
        Calendar calendar = Calendar.getInstance(), current = Calendar.getInstance();
        if(!json.equals("NULL")) { //Jeigu yra išsaugotas tvarkaraščio atnaujinimo laikas
            Gson gson = new Gson();
            Type calendarType = new TypeToken<Calendar>(){}.getType();
            calendar = gson.fromJson(json, calendarType); //Nuskaitome išsaugotą laiką
            while(calendar.before(current))
                calendar.add(Calendar.DATE, 7);
        } else { //Jeigu nėra išsaugoto tvarkaraščio atnaujinimo laiko
            int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
            int final_day = ((day_of_week >= 2 && day_of_week <= 6) ? ((day_of_week % 2 == 0) ? 1 : 7) : 7);
            while(calendar.get(Calendar.DAY_OF_WEEK) != final_day)
                calendar.add(Calendar.DATE, 1);
        }

        Gson gson = new Gson(); //Įrašome pamokų atnaujinimo laiką
        String laikoString = gson.toJson(current);
        mPrefs.edit().putString("autoUpdateTime", laikoString).apply();

        //Nustatome priminimą atnaujinti pamokas
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent notifyIntent = new Intent(context, MyReceiver.class);
        notifyIntent.putExtra("task", "download");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 98, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
    /*----------------------------------------------------------------------------------------*/
}
