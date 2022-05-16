package org.techtown.rssimeasureapp;

import android.content.Context;
import android.content.SharedPreferences;

// Get A and n values of KF from SharedPreferences
public class PrefManager {
    private static final String PREFERENCES_NAME = "KFvar";

    private static SharedPreferences getPrefences(Context context){
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
    public static void setString(Context context, String key, String value){
        SharedPreferences prefs = getPrefences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    // if there is no value of key, just return default value.
    public static String getString(Context context, String key, String defaultValue){
        SharedPreferences prefs = getPrefences(context);
        return prefs.getString(key, defaultValue);
    }

    public static void setBoolean(Context context, String key, boolean value){
        SharedPreferences prefs = getPrefences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue){
        SharedPreferences prefs = getPrefences(context);
        return prefs.getBoolean(key, defaultValue);
    }
}
