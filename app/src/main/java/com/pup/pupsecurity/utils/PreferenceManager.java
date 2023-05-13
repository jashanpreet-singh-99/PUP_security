package com.pup.pupsecurity.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton class for SharedPreferences
 */
public class PreferenceManager {

    private static final String APP_SETTINGS = "APP_SETTINGS";

    private PreferenceManager() {}

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
    }//getPreferences

    public static boolean getBoolean(Context context, String KEY) {
        return getPreferences(context).getBoolean(KEY, false);
    }//getBoolean

    public static void setBoolean(Context context, String KEY, Boolean value) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(KEY, value);
        editor.apply();
    }//setBoolean

    public static String getString(Context context, String KEY) {
        return getPreferences(context).getString(KEY, null);
    }//getString

    public static void setString(Context context, String KEY, String value) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY, value);
        editor.apply();
    }//setString

    public static int getInt(Context context, String KEY) {
        return getPreferences(context).getInt(KEY, -1);
    }//getString

    public static void setInt(Context context, String KEY, int value) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(KEY, value);
        editor.apply();
    }//setString

}
