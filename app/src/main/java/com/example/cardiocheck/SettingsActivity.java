package com.example.cardiocheck;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.cardiocheck.models.BloodPressureReading;
import com.example.cardiocheck.models.User;
import com.example.cardiocheck.utils.SharedPreferencesHelper;
import com.example.cardiocheck.utils.SmartNotificationManager;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Actividad UNIFICADA de Configuración y Perfil.
 */
public class SettingsActivity extends AppCompatActivity {

    // --- Helpers ---
    private DatabaseHelper db;
    private User currentUser;

    // --- Vistas de la UI (Sección Perfil) ---
    private LinearLayout layoutProfileHeader, layoutProfileContent;
    private ImageView ivExpandProfile;
    private TextInputEditText etFullName, etAge, etHeight, etWeight;
    private TextInputEditText etMedicalConditions, etMedications, etDoctorName, etEmergencyContact;
    private AutoCompleteTextView spinnerGender;
    private CardView cardBMI;
    private TextView tvBMIValue, tvBMICategory, tvProfileStatus;

    // --- Vistas de la UI (Sección Ajustes) ---
    private SwitchMaterial switchDarkMode, switchSmartAnalysis;
    private LinearLayout layoutReminders, layoutExport, layoutClearData;
    private TextView tvReminderStatus;
    private Button btnLogout;
    private ImageButton btnBack, btnSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = new DatabaseHelper(this);

        initializeViews();
        setupEventListeners();

        // Cargar tanto los datos del perfil como los ajustes de la app
        loadAllData();
    }

    private void initializeViews() {
        // --- Header ---
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);

        // --- Vistas del Perfil ---
        layoutProfileHeader = findViewById(R.id.layoutProfileHeader);
        layoutProfileContent = findViewById(R.id.layoutProfileContent);
        ivExpandProfile = findViewById(R.id.ivExpandProfile);
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        spinnerGender = findViewById(R.id.spinnerGender);
        etMedicalConditions = findViewById(R.id.etMedicalConditions);
        etMedications = findViewById(R.id.etMedications);
        etDoctorName = findViewById(R.id.etDoctorName);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        cardBMI = findViewById(R.id.cardBMI);
        tvBMIValue = findViewById(R.id.tvBMIValue);
        tvBMICategory = findViewById(R.id.tvBMICategory);
        tvProfileStatus = findViewById(R.id.tvProfileStatus);

        // --- Vistas de Ajustes ---
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchSmartAnalysis = findViewById(R.id.switchSmartAnalysis);
        layoutReminders = findViewById(R.id.layoutReminders);
        tvReminderStatus = findViewById(R.id.tvReminderStatus);
        layoutExport = findViewById(R.id.layoutExport);
        layoutClearData = findViewById(R.id.layoutClearData);
        btnLogout = findViewById(R.id.btnLogout);

        // Configurar Spinner de Género
        String[] genders = {"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        spinnerGender.setAdapter(genderAdapter);
    }

    private void setupEventListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());

        // Listener para expandir/colapsar el perfil
        layoutProfileHeader.setOnClickListener(v -> toggleProfileSection());

        // --- Listeners de Ajustes ---
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesHelper.setDarkModeEnabled(this, isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        switchSmartAnalysis.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesHelper.setSmartAnalysisEnabled(this, isChecked);
            Toast.makeText(this, isChecked ? "Análisis inteligente activado" : "Análisis inteligente desactivado", Toast.LENGTH_SHORT).show();
            if (isChecked) {
                new SmartNotificationManager(this).setupIntelligentReminders(currentUser.getEmail());
            }
        });

        layoutReminders.setOnClickListener(v -> startActivity(new Intent(this, RemindersActivity.class)));
        layoutExport.setOnClickListener(v -> exportData());
        layoutClearData.setOnClickListener(v -> showClearDataDialog());
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void toggleProfileSection() {
        boolean isVisible = layoutProfileContent.getVisibility() == View.VISIBLE;
        layoutProfileContent.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        ivExpandProfile.setImageResource(isVisible ? R.drawable.ic_expand_more : R.drawable.ic_expand_less);
    }

    private void loadAllData() {
        // --- Cargar Perfil de Usuario (lógica de ProfileActivity) ---
        String userEmail = SharedPreferencesHelper.getUserEmail(this);
        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = db.getUserByEmail(userEmail);
        if (currentUser == null) {
            Toast.makeText(this, "Error: No se encontraron datos del perfil.", Toast.LENGTH_SHORT).show();
            // Crear un usuario vacío para evitar crashes
            currentUser = new User();
            currentUser.setEmail(userEmail);
        }

        etFullName.setText(currentUser.getFullName());
        if (currentUser.getAge() > 0) etAge.setText(String.valueOf(currentUser.getAge()));
        if (currentUser.getHeight() > 0) etHeight.setText(String.format(Locale.US, "%.1f", currentUser.getHeight()));
        if (currentUser.getWeight() > 0) etWeight.setText(String.format(Locale.US, "%.1f", currentUser.getWeight()));

        spinnerGender.setText(currentUser.getGender(), false);
        etMedicalConditions.setText(currentUser.getMedicalConditions());
        etMedications.setText(currentUser.getMedications());
        etDoctorName.setText(currentUser.getDoctorName());
        etEmergencyContact.setText(currentUser.getEmergencyContact());

        // --- Cargar Ajustes (lógica de SettingsActivity) ---
        switchDarkMode.setChecked(SharedPreferencesHelper.isDarkModeEnabled(this));
        switchSmartAnalysis.setChecked(SharedPreferencesHelper.isSmartAnalysisEnabled(this));

        // Actualizar estados visuales
        updateProfileStatus();
        updateReminderStatus();
        calculateAndShowBMI();
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("El nombre no puede estar vacío.");
            etFullName.requestFocus();
            return;
        }

        int age = 0;
        try { age = Integer.parseInt(etAge.getText().toString()); } catch (NumberFormatException e) {}

        float height = 0.0f;
        try { height = Float.parseFloat(etHeight.getText().toString().replace(',', '.')); } catch (NumberFormatException e) {}

        float weight = 0.0f;
        try { weight = Float.parseFloat(etWeight.getText().toString().replace(',', '.')); } catch (NumberFormatException e) {}

        currentUser.setFullName(fullName);
        currentUser.setAge(age);
        currentUser.setHeight(height);
        currentUser.setWeight(weight);
        currentUser.setGender(spinnerGender.getText().toString());
        currentUser.setMedicalConditions(etMedicalConditions.getText().toString().trim());
        currentUser.setMedications(etMedications.getText().toString().trim());
        currentUser.setDoctorName(etDoctorName.getText().toString().trim());
        currentUser.setEmergencyContact(etEmergencyContact.getText().toString().trim());

        boolean isUpdated = db.updateUserProfile(currentUser);

        if (isUpdated) {
            SharedPreferencesHelper.setUserFullName(this, fullName); // Actualizar el nombre en sesión
            Toast.makeText(this, "Perfil guardado con éxito.", Toast.LENGTH_SHORT).show();
            // Recargar los estados visuales que dependen del perfil
            updateProfileStatus();
            calculateAndShowBMI();
        } else {
            Toast.makeText(this, "Error: No se pudo guardar el perfil.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Métodos de UI y Lógica Auxiliar (Movidos de ambas clases) ---

    private void calculateAndShowBMI() {
        // ... (lógica idéntica a la de ProfileActivity)
        if (currentUser.getHeight() > 0 && currentUser.getWeight() > 0) {
            float heightInMeters = currentUser.getHeight() / 100;
            float bmi = currentUser.getWeight() / (heightInMeters * heightInMeters);

            tvBMIValue.setText(String.format(Locale.US, "%.1f", bmi));
            String category;
            int colorRes;

            if (bmi < 18.5) {
                category = "Bajo peso";
                colorRes = R.color.bp_stage1;
            } else if (bmi < 25) {
                category = "Peso normal";
                colorRes = R.color.bp_optimal;
            } else if (bmi < 30) {
                category = "Sobrepeso";
                colorRes = R.color.bp_stage1;
            } else {
                category = "Obesidad";
                colorRes = R.color.bp_stage2;
            }
            tvBMICategory.setText(category);
            tvBMICategory.setTextColor(ContextCompat.getColor(this, colorRes));
            cardBMI.setVisibility(View.VISIBLE);
        } else {
            cardBMI.setVisibility(View.GONE);
        }
    }

    private void updateProfileStatus() {
        // ... (lógica idéntica a la de SettingsActivity original)
        if (currentUser != null && currentUser.isProfileComplete()) {
            tvProfileStatus.setText("Completo ✓");
            tvProfileStatus.setTextColor(ContextCompat.getColor(this, R.color.bp_optimal));
        } else {
            tvProfileStatus.setText("Incompleto");
            tvProfileStatus.setTextColor(ContextCompat.getColor(this, R.color.error));
        }
    }

    private void updateReminderStatus() {
        // ... (lógica idéntica a la de SettingsActivity original)
        if (SharedPreferencesHelper.isReminderEnabled(this)) {
            int hour = SharedPreferencesHelper.getReminderHour(this);
            int minute = SharedPreferencesHelper.getReminderMinute(this);
            tvReminderStatus.setText(String.format(Locale.getDefault(), "Activo - %02d:%02d", hour, minute));
            tvReminderStatus.setTextColor(ContextCompat.getColor(this, R.color.bp_optimal));
        } else {
            tvReminderStatus.setText("No configurado");
            tvReminderStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void exportData() {
        // ... (lógica idéntica a la de SettingsActivity original)
        List<BloodPressureReading> readings = db.getAllReadings(currentUser.getEmail());
        if (readings.isEmpty()) {
            Toast.makeText(this, "No hay datos para exportar.", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            File pdfFile = new PDFGenerator(this).generateAdvancedPDF(readings, currentUser.getEmail());
            runOnUiThread(() -> {
                if (pdfFile != null) {
                    // Lógica para compartir el archivo...
                    Toast.makeText(this, "Reporte generado: " + pdfFile.getName(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Error al generar el reporte.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void showClearDataDialog() {
        // ... (lógica idéntica a la de SettingsActivity original)
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Eliminar Datos")
                .setMessage("Esta acción eliminará permanentemente todas tus mediciones. ¿Estás seguro?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    int deletedCount = db.clearAllReadings(currentUser.getEmail());
                    Toast.makeText(this, "Se eliminaron " + deletedCount + " mediciones.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showLogoutDialog() {
        // ... (lógica idéntica a la de SettingsActivity original)
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Cerrar Sesión", (dialog, which) -> {
                    SharedPreferencesHelper.logout(this);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cuando volvemos a esta pantalla (ej. desde recordatorios), actualizamos el estado.
        updateReminderStatus();
    }
}
