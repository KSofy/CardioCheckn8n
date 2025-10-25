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
 * DatabaseHelper: gestiona usuarios con perfil médico completo y lecturas de presión arterial.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cardiocheck.db";
    private static final int DB_VERSION = 2; // Incrementamos versión para agregar nuevos campos

    // Tabla usuarios - campos expandidos
    public static final String TABLE_USERS = "users";
    private static final String U_ID = "id";
    private static final String U_NAME = "full_name";
    private static final String U_EMAIL = "email";
    private static final String U_PASSWORD = "password";
    // Nuevos campos de perfil médico
    private static final String U_AGE = "age";
    private static final String U_HEIGHT = "height";
    private static final String U_WEIGHT = "weight";
    private static final String U_GENDER = "gender";
    private static final String U_MEDICAL_CONDITIONS = "medical_conditions";
    private static final String U_MEDICATIONS = "medications";
    private static final String U_DOCTOR_NAME = "doctor_name";
    private static final String U_EMERGENCY_CONTACT = "emergency_contact";

    // Tabla lecturas
    public static final String TABLE_READINGS = "blood_pressure_readings";
    private static final String R_ID = "id";
    private static final String R_EMAIL = "email";
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
                U_PASSWORD + " TEXT NOT NULL, " +
                U_AGE + " INTEGER DEFAULT 0, " +
                U_HEIGHT + " REAL DEFAULT 0.0, " +
                U_WEIGHT + " REAL DEFAULT 0.0, " +
                U_GENDER + " TEXT DEFAULT '', " +
                U_MEDICAL_CONDITIONS + " TEXT DEFAULT '', " +
                U_MEDICATIONS + " TEXT DEFAULT '', " +
                U_DOCTOR_NAME + " TEXT DEFAULT '', " +
                U_EMERGENCY_CONTACT + " TEXT DEFAULT '')";

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
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + U_AGE + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + U_HEIGHT + " REAL DEFAULT 0.0");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + U_WEIGHT + " REAL DEFAULT 0.0");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + U_GENDER + " TEXT DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + U_MEDICAL_CONDITIONS + " TEXT DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + U_MEDICATIONS + " TEXT DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + U_DOCTOR_NAME + " TEXT DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + U_EMERGENCY_CONTACT + " TEXT DEFAULT ''");
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_READINGS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
                onCreate(db);
            }
        }
    }

    // === MÉTODOS DE USUARIOS ===

    public boolean registerUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(U_NAME, user.getFullName());
        cv.put(U_EMAIL, user.getEmail());
        cv.put(U_PASSWORD, user.getPassword());
        cv.put(U_AGE, user.getAge());
        cv.put(U_HEIGHT, user.getHeight());
        cv.put(U_WEIGHT, user.getWeight());
        cv.put(U_GENDER, user.getGender());
        cv.put(U_MEDICAL_CONDITIONS, user.getMedicalConditions());
        cv.put(U_MEDICATIONS, user.getMedications());
        cv.put(U_DOCTOR_NAME, user.getDoctorName());
        cv.put(U_EMERGENCY_CONTACT, user.getEmergencyContact());
        long id = db.insert(TABLE_USERS, null, cv);
        return id != -1;
    }

    public User login(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null, U_EMAIL + "=? AND " + U_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        try {
            if (c.moveToFirst()) {
                return mapUser(c);
            }
            return null;
        } finally {
            c.close();
        }
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null, U_EMAIL + "=?",
                new String[]{email}, null, null, null);
        try {
            if (c.moveToFirst()) {
                return mapUser(c);
            }
            return null;
        } finally {
            c.close();
        }
    }

    public boolean updateUserProfile(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(U_NAME, user.getFullName());
        cv.put(U_AGE, user.getAge());
        cv.put(U_HEIGHT, user.getHeight());
        cv.put(U_WEIGHT, user.getWeight());
        cv.put(U_GENDER, user.getGender());
        cv.put(U_MEDICAL_CONDITIONS, user.getMedicalConditions());
        cv.put(U_MEDICATIONS, user.getMedications());
        cv.put(U_DOCTOR_NAME, user.getDoctorName());
        cv.put(U_EMERGENCY_CONTACT, user.getEmergencyContact());

        int rowsAffected = db.update(TABLE_USERS, cv, U_EMAIL + "=?",
                new String[]{user.getEmail()});
        return rowsAffected > 0;
    }

    private User mapUser(Cursor c) {
        User user = new User();
        user.setId(c.getLong(c.getColumnIndexOrThrow(U_ID)));
        user.setFullName(c.getString(c.getColumnIndexOrThrow(U_NAME)));
        user.setEmail(c.getString(c.getColumnIndexOrThrow(U_EMAIL)));
        user.setPassword(c.getString(c.getColumnIndexOrThrow(U_PASSWORD)));

        try {
            user.setAge(c.getInt(c.getColumnIndexOrThrow(U_AGE)));
            user.setHeight(c.getFloat(c.getColumnIndexOrThrow(U_HEIGHT)));
            user.setWeight(c.getFloat(c.getColumnIndexOrThrow(U_WEIGHT)));
            user.setGender(c.getString(c.getColumnIndexOrThrow(U_GENDER)));
            user.setMedicalConditions(c.getString(c.getColumnIndexOrThrow(U_MEDICAL_CONDITIONS)));
            user.setMedications(c.getString(c.getColumnIndexOrThrow(U_MEDICATIONS)));
            user.setDoctorName(c.getString(c.getColumnIndexOrThrow(U_DOCTOR_NAME)));
            user.setEmergencyContact(c.getString(c.getColumnIndexOrThrow(U_EMERGENCY_CONTACT)));
        } catch (Exception e) {
            user.setAge(0);
            user.setHeight(0.0f);
            user.setWeight(0.0f);
            user.setGender("");
            user.setMedicalConditions("");
            user.setMedications("");
            user.setDoctorName("");
            user.setEmergencyContact("");
        }

        return user;
    }

    // === MÉTODOS DE LECTURAS ===

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

    // --- ESTA ES LA ÚNICA CORRECCIÓN ---
    // He renombrado el método para evitar el error.
    public void updateReadingRecommendation(long readingId, String recommendation) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(R_AI, recommendation);
        db.update(TABLE_READINGS, cv, R_ID + "=?", new String[]{String.valueOf(readingId)});
    }

    public BloodPressureReading getReadingById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_READINGS, null, R_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (c.moveToFirst()) {
                return mapReading(c);
            }
            return null;
        } finally {
            c.close();
        }
    }

    public void clearInvalidAIRecommendations() {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(R_AI, "");
        db.update(TABLE_READINGS, cv,
                R_AI + " LIKE '%clave de openai%' OR " +
                        R_AI + " LIKE '%configura tu clave%' OR " +
                        R_AI + " LIKE '%no hay una clave de openai%' OR " +
                        R_AI + " LIKE '%ingresa tu clave de openai%'", null);
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

    public int clearAllReadings(String userEmail) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_READINGS, R_EMAIL + "=?", new String[]{userEmail});
    }
}