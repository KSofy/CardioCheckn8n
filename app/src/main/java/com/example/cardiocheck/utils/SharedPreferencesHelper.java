package com.example.cardiocheck.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper para gestionar todos los datos de sesión y configuración en SharedPreferences.
 */
public class SharedPreferencesHelper {

    private static final String PREF_NAME = "CardioCheckPrefs";

    // --- Claves para los datos ---
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_FULL_NAME = "userFullName";

    private static final String KEY_REMINDER_ENABLED = "reminderEnabled";
    private static final String KEY_REMINDER_HOUR = "reminderHour";
    private static final String KEY_REMINDER_MINUTE = "reminderMinute";

    private static final String KEY_OPENAI_API_KEY = "openaiApiKey";

    // --- NUEVAS CLAVES PARA AJUSTES ---
    private static final String KEY_DARK_MODE_ENABLED = "darkModeEnabled";
    private static final String KEY_SMART_ANALYSIS_ENABLED = "smartAnalysisEnabled";


    // === MÉTODOS DE SESIÓN ===

    public static void login(Context context, String email, String fullName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.apply();
    }

    public static void logout(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Borra todos los datos de la sesión
        editor.apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static String getUserEmail(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public static String getUserFullName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_FULL_NAME, "");
    }

    public static void setUserFullName(Context context, String fullName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FULL_NAME, fullName);
        editor.apply();
    }


    // === MÉTODOS DE OPENAI API KEY ===

    public static void setOpenAIKey(Context context, String apiKey) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_OPENAI_API_KEY, apiKey);
        editor.apply();
    }

    public static String getOpenAIKey(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // Devuelve una cadena vacía en lugar de null para evitar NullPointerExceptions
        return sharedPreferences.getString(KEY_OPENAI_API_KEY, "");
    }


    // === MÉTODOS DE RECORDATORIOS ===

    public static void setReminderEnabled(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMINDER_ENABLED, enabled);
        editor.apply();
    }

    public static boolean isReminderEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_REMINDER_ENABLED, false);
    }

    public static void setReminderTime(Context context, int hour, int minute) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_REMINDER_HOUR, hour);
        editor.putInt(KEY_REMINDER_MINUTE, minute);
        editor.apply();
    }

    public static int getReminderHour(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_REMINDER_HOUR, 8); // Default 8 AM
    }

    public static int getReminderMinute(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_REMINDER_MINUTE, 0); // Default :00
    }

    // === MÉTODOS DE AJUSTES (MODO OSCURO Y ANÁLISIS INTELIGENTE) ===

    public static void setDarkModeEnabled(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_DARK_MODE_ENABLED, enabled);
        editor.apply();
    }

    public static boolean isDarkModeEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_DARK_MODE_ENABLED, false);
    }

    public static void setSmartAnalysisEnabled(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SMART_ANALYSIS_ENABLED, enabled);
        editor.apply();
    }

    public static boolean isSmartAnalysisEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_SMART_ANALYSIS_ENABLED, false);
    }
}
