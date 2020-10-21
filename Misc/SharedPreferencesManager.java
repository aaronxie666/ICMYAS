package icn.icmyas.Misc;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Author:  Bradley Wilson
 * Date: 18/07/2017
 * Package: icn.icmyas.Misc
 * Project Name: ICMYAS
 */
public class SharedPreferencesManager {

    private static final String SHARED_PREFS = "shared_prefs";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    }

    public static boolean getBoolean(Context context, String key) {
        return getSharedPreferences(context).getBoolean(key, false);
    }

    public static int getInt(Context context, String key) {
        return getSharedPreferences(context).getInt(key, -1);
    }

    public static void setInt(Context context, String key, int intValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(key, intValue);
        editor.apply();
    }

    public static void setBoolean(Context context, String key, boolean booleanValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key, booleanValue);
        editor.apply();
    }

    public static String getString(Context context, String key) {
        return getSharedPreferences(context).getString(key, "");
    }

    public static void setString(Context context, String key, String stringValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, stringValue);
        editor.apply();
    }

    public static void setDouble(Context context, String key, double doubleValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(key, Double.doubleToRawLongBits(doubleValue));
        editor.apply();
    }

    public static double getDouble(Context context, final String key) {
        return Double.longBitsToDouble(getSharedPreferences(context).getLong(key, Double.doubleToRawLongBits(0.01)));
    }
}