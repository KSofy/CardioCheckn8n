package com.example.cardiocheck;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.cardiocheck.models.User;
import com.example.cardiocheck.utils.SharedPreferencesHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    // --- Helpers ---
    private DatabaseHelper db;
    private User currentUser;

    // --- Vistas de la UI ---
    private TextInputEditText etFullName, etAge, etHeight, etWeight;
    private TextInputEditText etMedicalConditions, etMedications, etDoctorName, etEmergencyContact;
    private AutoCompleteTextView spinnerGender;

    private CardView cardBMI;
    private TextView tvBMIValue, tvBMICategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);

        initializeViews();
        setupEventListeners();

        loadUserProfile();
    }

    private void initializeViews() {
        // --- Campos de texto ---
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        spinnerGender = findViewById(R.id.spinnerGender);
        etMedicalConditions = findViewById(R.id.etMedicalConditions);
        etMedications = findViewById(R.id.etMedications);
        etDoctorName = findViewById(R.id.etDoctorName);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);

        // --- Tarjeta BMI ---
        cardBMI = findViewById(R.id.cardBMI);
        tvBMIValue = findViewById(R.id.tvBMIValue);
        tvBMICategory = findViewById(R.id.tvBMICategory);

        // --- Configurar Spinner ---
        String[] genders = {"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        spinnerGender.setAdapter(genderAdapter);
    }

    private void setupEventListeners() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnSave = findViewById(R.id.btnSave);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnBack.setOnClickListener(v -> finish()); // Cierra la actividad y vuelve a la anterior
        btnSave.setOnClickListener(v -> saveProfile());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void loadUserProfile() {
        String userEmail = SharedPreferencesHelper.getUserEmail(this);
        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = db.getUserByEmail(userEmail);
        if (currentUser == null) {
            Toast.makeText(this, "Error: No se encontraron los datos del perfil.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etFullName.setText(currentUser.getFullName());

        if (currentUser.getAge() > 0) {
            etAge.setText(String.valueOf(currentUser.getAge()));
        }
        if (currentUser.getHeight() > 0) {
            etHeight.setText(String.format(Locale.US, "%.1f", currentUser.getHeight()));
        }
        if (currentUser.getWeight() > 0) {
            etWeight.setText(String.format(Locale.US, "%.1f", currentUser.getWeight()));
        }

        if (!TextUtils.isEmpty(currentUser.getGender())) {
            spinnerGender.setText(currentUser.getGender(), false);
        }
        if (!TextUtils.isEmpty(currentUser.getMedicalConditions())) {
            etMedicalConditions.setText(currentUser.getMedicalConditions());
        }
        if (!TextUtils.isEmpty(currentUser.getMedications())) {
            etMedications.setText(currentUser.getMedications());
        }
        if (!TextUtils.isEmpty(currentUser.getDoctorName())) {
            etDoctorName.setText(currentUser.getDoctorName());
        }
        if (!TextUtils.isEmpty(currentUser.getEmergencyContact())) {
            etEmergencyContact.setText(currentUser.getEmergencyContact());
        }

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
        try { height = Float.parseFloat(etHeight.getText().toString()); } catch (NumberFormatException e) {}

        float weight = 0.0f;
        try { weight = Float.parseFloat(etWeight.getText().toString()); } catch (NumberFormatException e) {}

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
            SharedPreferencesHelper.setUserFullName(this, fullName);
            Toast.makeText(this, "Perfil guardado con éxito.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error: No se pudo guardar el perfil.", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateAndShowBMI() {
        if (currentUser.getHeight() > 0 && currentUser.getWeight() > 0) {
            float heightInMeters = currentUser.getHeight() / 100;
            float bmi = currentUser.getWeight() / (heightInMeters * heightInMeters);

            tvBMIValue.setText(String.format(Locale.US, "%.1f", bmi));

            String category;
            int color;

            if (bmi < 18.5) {
                category = "Bajo peso";
                color = R.color.bp_stage1;
            } else if (bmi < 25) {
                category = "Peso normal";
                color = R.color.bp_optimal;
            } else if (bmi < 30) {
                category = "Sobrepeso";
                color = R.color.bp_stage1;
            } else {
                category = "Obesidad";
                color = R.color.bp_stage2;
            }
            tvBMICategory.setText(category);
            tvBMICategory.setTextColor(getResources().getColor(color, getTheme()));
            cardBMI.setVisibility(View.VISIBLE);
        } else {
            cardBMI.setVisibility(View.GONE);
        }
    }
}
