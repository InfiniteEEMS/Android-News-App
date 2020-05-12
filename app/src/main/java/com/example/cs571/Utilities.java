package com.example.cs571;

import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class Utilities {

    public static String homepage_url = "https://erickhw5node.wl.r.appspot.com/getGuardianNewsHomePage";
    public static String other_url = "https://erickhw5node.wl.r.appspot.com/getGuardianNews";
    public static String trend_url = "https://erickhw5node.wl.r.appspot.com/getTrend";




    public static boolean checkNewsInPreference(String target_id) {
        try {
            SharedPreferences pref = MyApplication.myGetAppContext().getSharedPreferences("favorite", 0);
            // find target obj and remove
            JSONObject ids = new JSONObject(pref.getString("ids", null));
            JSONArray ids_arr = ids.getJSONArray("ids_arr");
            for (int i = 0; i < ids_arr.length(); i++) {
                JSONObject n = ids_arr.getJSONObject(i);
                if (n.getString("id").equals(target_id)) {
                    Log.d("PREFERENCE", "fetching news find it already in preference: " + target_id + " in preference");
                    ids_arr.remove(i);
                    return true;
                }
            }
            return false;

        } catch (JSONException e) {
            Log.e("JSON", "Error in transform string to json in sharePreference ");
            e.printStackTrace();
        }
        return false;
    }


    public static void printCurrentPreference() {
        int find = 0;
        try {
            SharedPreferences pref = MyApplication.myGetAppContext().getSharedPreferences("favorite", 0);
            // find target obj and remove
            JSONObject ids = new JSONObject(pref.getString("ids", null));
            JSONArray ids_arr = ids.getJSONArray("ids_arr");
            for (int i = 0; i < ids_arr.length(); i++) {
                find = 1;
                JSONObject n = ids_arr.getJSONObject(i);
                Log.d("PREFERENCE", "      (entry) => " + n.getString("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (find == 0) {
            Log.d("PREFERENCE", "      (entry) => " + " NO ENTRY");
        }
    }


    public static void updateNewsListFromPreference(List<NewsItem> news){
        for(int i=0; i<news.size(); i++){
            if(Utilities.checkNewsInPreference(news.get(i).getId())){
                news.get(i).setMarked(true);
            }else{
                news.get(i).setMarked(false);
            }
        }
    };

    public static boolean addItemIntoPreference(NewsItem cur) {
        try {
            SharedPreferences pref = MyApplication.myGetAppContext().getSharedPreferences("favorite", 0);
            SharedPreferences.Editor editor = pref.edit();

            JSONObject ids = new JSONObject(pref.getString("ids", null));
            JSONArray ids_arr = ids.getJSONArray("ids_arr");
            ids_arr.put(cur.myToJSONObj());
            JSONObject res = new JSONObject();
            res.put("ids_arr", ids_arr);
            // editor write
            editor.putString("ids", res.toString());
            editor.commit();
            return true;

        } catch (JSONException e) {
            Log.e("JSON", "Error in transform string to json in sharePreference ");
            e.printStackTrace();
        }
        return false;
    }


    public static boolean removeItemFromPreference(String target_id) {
        try {
            SharedPreferences pref = MyApplication.myGetAppContext().getSharedPreferences("favorite", 0);
            SharedPreferences.Editor editor = pref.edit();
            // find target obj and remove
            JSONObject ids = new JSONObject(pref.getString("ids", null));
            JSONArray ids_arr = ids.getJSONArray("ids_arr");
            for (int i = 0; i < ids_arr.length(); i++) {
                JSONObject n = ids_arr.getJSONObject(i);
                if (n.getString("id").equals(target_id)) {
                    ids_arr.remove(i);
                }
            }
            // construct new obj
            JSONObject res = new JSONObject();
            res.put("ids_arr", ids_arr);
            // editor write
            editor.putString("ids", res.toString());
            editor.commit();
            Utilities.printCurrentPreference();
            return true;

        } catch (JSONException e) {
            Log.e("JSON", "Error in transform string to json in sharePreference ");
            e.printStackTrace();
        }
        return false;
    }



/////////////////////////  String

    public static boolean isEmptyString(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String timeConvertToDiff(String t){
        // String t = "2020-05-02T06:14:36Z";
        // DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        ZonedDateTime tmp = ZonedDateTime.parse(t);
        ZonedDateTime target = tmp.withZoneSameInstant( ZoneId.of( "America/Los_Angeles" ) );

        ZonedDateTime current = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));

        Duration duration = Duration.between(target, current);

        if(duration.toDays()>=1){
            return duration.toDays() + "d ago";
        }else if(duration.toHours() >= 1){
            return duration.toHours() + "h ago";
        }else if(duration.toMinutes() >= 1){
            return duration.toMinutes() + "m ago";
        }else{
            return duration.getSeconds() + "s ago";
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String timeConvertToDDMMYYYY(String t){
        // String t = "2020-05-02T06:14:36Z";
        // DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        ZonedDateTime tmp = ZonedDateTime.parse(t);
        ZonedDateTime target = tmp.withZoneSameInstant( ZoneId.of( "America/Los_Angeles" ) );

        String res =  Integer.toString(target.getDayOfMonth());
        if(res.length()<2){
            res = "0" + res;
        }
        res = res + " " + target.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        res = res + " " + target.getYear();
        return res;
    }



}