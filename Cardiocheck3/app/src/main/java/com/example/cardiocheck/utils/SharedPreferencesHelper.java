package com.example.cardiocheck.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper para gestionar sesión de usuario y clave de OpenAI.
 */
public class SharedPreferencesHelper {
    private static final String PREF_NAME = "cardiocheck_prefs";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_OPENAI_API = "openai_api_key";

    public static void setLoggedIn(Context context, boolean loggedIn) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_LOGGED_IN, false);
    }

    public static void setUser(Context context, String name, String email) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_USER_NAME, name).putString(KEY_USER_EMAIL, email).apply();
    }

    public static String getUserEmail(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_USER_EMAIL, null);
    }

    public static String getUserName(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_USER_NAME, "");
    }

    public static void saveOpenAIKey(Context context, String apiKey) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_OPENAI_API, apiKey).apply();
    }

    public static String getOpenAIKey(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_OPENAI_API, null);
    }

    public static void logout(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}

