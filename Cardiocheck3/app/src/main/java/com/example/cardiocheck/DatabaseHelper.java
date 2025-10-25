package com.example.cardiocheck;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cardiocheck.models.BloodPressureReading;
import com.example.cardiocheck.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper: gestiona usuarios y lecturas de presión arterial.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cardiocheck.db";
    private static final int DB_VERSION = 1;

    // Tabla usuarios
    public static final String TABLE_USERS = "users";
    private static final String U_ID = "id";
    private static final String U_NAME = "full_name";
    private static final String U_EMAIL = "email";
    private static final String U_PASSWORD = "password";

    // Tabla lecturas
    public static final String TABLE_READINGS = "blood_pressure_readings";
    private static final String R_ID = "id";
    private static final String R_EMAIL = "email"; // dueño (relación por email)
    private static final String R_SYSTOLIC = "systolic";
    private static final String R_DIASTOLIC = "diastolic";
    private static final String R_PULSE = "pulse";
    private static final String R_TIMESTAMP = "timestamp";
    private static final String R_AI = "ai_recommendation";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                U_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                U_NAME + " TEXT NOT NULL, " +
                U_EMAIL + " TEXT UNIQUE NOT NULL, " +
                U_PASSWORD + " TEXT NOT NULL)";

        String createReadings = "CREATE TABLE " + TABLE_READINGS + " (" +
                R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                R_EMAIL + " TEXT NOT NULL, " +
                R_SYSTOLIC + " INTEGER NOT NULL, " +
                R_DIASTOLIC + " INTEGER NOT NULL, " +
                R_PULSE + " INTEGER NOT NULL, " +
                R_TIMESTAMP + " INTEGER NOT NULL, " +
                R_AI + " TEXT, " +
                "FOREIGN KEY(" + R_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + U_EMAIL + ") ON DELETE CASCADE)";

        db.execSQL(createUsers);
        db.execSQL(createReadings);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_READINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Usuarios
    public boolean registerUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(U_NAME, user.getFullName());
        cv.put(U_EMAIL, user.getEmail());
        cv.put(U_PASSWORD, user.getPassword());
        long id = db.insert(TABLE_USERS, null, cv);
        return id != -1;
    }

    public User login(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null, U_EMAIL + "=? AND " + U_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        try {
            if (c.moveToFirst()) {
                User u = new User();
                u.setId(c.getLong(c.getColumnIndexOrThrow(U_ID)));
                u.setFullName(c.getString(c.getColumnIndexOrThrow(U_NAME)));
                u.setEmail(c.getString(c.getColumnIndexOrThrow(U_EMAIL)));
                u.setPassword(c.getString(c.getColumnIndexOrThrow(U_PASSWORD)));
                return u;
            }
            return null;
        } finally {
            c.close();
        }
    }

    // Lecturas
    public long insertReading(BloodPressureReading r) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(R_EMAIL, r.getEmail());
        cv.put(R_SYSTOLIC, r.getSystolic());
        cv.put(R_DIASTOLIC, r.getDiastolic());
        cv.put(R_PULSE, r.getPulse());
        cv.put(R_TIMESTAMP, r.getTimestamp());
        cv.put(R_AI, r.getAiRecommendation());
        return db.insert(TABLE_READINGS, null, cv);
    }

    public List<BloodPressureReading> getLastReadings(String email, int limit) {
        SQLiteDatabase db = getReadableDatabase();
        List<BloodPressureReading> result = new ArrayList<>();
        Cursor c = db.query(TABLE_READINGS, null, R_EMAIL + "=?", new String[]{email},
                null, null, R_TIMESTAMP + " DESC", String.valueOf(limit));
        try {
            while (c.moveToNext()) {
                result.add(mapReading(c));
            }
        } finally { c.close(); }
        return result;
    }

    public List<BloodPressureReading> getAllReadings(String email) {
        SQLiteDatabase db = getReadableDatabase();
        List<BloodPressureReading> result = new ArrayList<>();
        Cursor c = db.query(TABLE_READINGS, null, R_EMAIL + "=?", new String[]{email},
                null, null, R_TIMESTAMP + " DESC");
        try {
            while (c.moveToNext()) {
                result.add(mapReading(c));
            }
        } finally { c.close(); }
        return result;
    }

    public int updateAIRecommendation(long readingId, String recommendation) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(R_AI, recommendation);
        return db.update(TABLE_READINGS, cv, R_ID + "=?", new String[]{String.valueOf(readingId)});
    }

    public BloodPressureReading getReadingById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_READINGS, null, R_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (c.moveToFirst()) return mapReading(c);
            return null;
        } finally { c.close(); }
    }

    // Limpia recomendaciones antiguas que pedían configurar clave de OpenAI
    public int clearInvalidAIRecommendations() {
        SQLiteDatabase db = getWritableDatabase();
        String where = "ai_recommendation IS NOT NULL AND (" +
                "LOWER(ai_recommendation) LIKE ? OR " +
                "LOWER(ai_recommendation) LIKE ? OR " +
                "LOWER(ai_recommendation) LIKE ? OR " +
                "LOWER(ai_recommendation) LIKE ?" +
                ")";
        String[] args = new String[]{
                "%clave de openai%",
                "%configura tu clave%",
                "%no hay una clave de openai%",
                "%ingresa tu clave de openai%"
        };
        ContentValues cv = new ContentValues();
        cv.putNull(R_AI);
        return db.update(TABLE_READINGS, cv, where, args);
    }

    private BloodPressureReading mapReading(Cursor c) {
        BloodPressureReading r = new BloodPressureReading();
        r.setId(c.getLong(c.getColumnIndexOrThrow(R_ID)));
        r.setEmail(c.getString(c.getColumnIndexOrThrow(R_EMAIL)));
        r.setSystolic(c.getInt(c.getColumnIndexOrThrow(R_SYSTOLIC)));
        r.setDiastolic(c.getInt(c.getColumnIndexOrThrow(R_DIASTOLIC)));
        r.setPulse(c.getInt(c.getColumnIndexOrThrow(R_PULSE)));
        r.setTimestamp(c.getLong(c.getColumnIndexOrThrow(R_TIMESTAMP)));
        r.setAiRecommendation(c.getString(c.getColumnIndexOrThrow(R_AI)));
        return r;
    }
}
