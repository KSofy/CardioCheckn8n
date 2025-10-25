package com.example.cardiocheck;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cardiocheck.models.User;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    // Vistas del formulario
    private TextInputEditText etFullName, etEmail, etPassword, etAge, etHeight, etWeight;
    private AutoCompleteTextView spinnerGender;
    private CheckBox cbDiabetes, cbHypertension;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = new DatabaseHelper(this);

        // Inicializar todas las vistas
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        spinnerGender = findViewById(R.id.spinnerGender);
        cbDiabetes = findViewById(R.id.cbDiabetes);
        cbHypertension = findViewById(R.id.cbHypertension);

        Button btnRegister = findViewById(R.id.btnDoRegister);

        // Configurar el spinner de género
        String[] genders = {"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        spinnerGender.setAdapter(genderAdapter);

        // Configurar el listener del botón
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        // --- 1. Validar campos básicos ---
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Nombre, email y contraseña son obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            etEmail.requestFocus();
            return;
        }
        if (pass.length() < 8) {
            etPassword.setError("La contraseña debe tener al menos 8 caracteres");
            etPassword.requestFocus();
            return;
        }

        // --- 2. Recoger datos adicionales (con valores por defecto si están vacíos) ---
        int age = 0;
        try {
            age = Integer.parseInt(etAge.getText().toString());
        } catch (NumberFormatException e) { // El campo está vacío
            age = 0;
        }

        float height = 0.0f;
        try {
            height = Float.parseFloat(etHeight.getText().toString());
        } catch (NumberFormatException e) {
            height = 0.0f;
        }

        float weight = 0.0f;
        try {
            weight = Float.parseFloat(etWeight.getText().toString());
        } catch (NumberFormatException e) {
            weight = 0.0f;
        }

        String gender = spinnerGender.getText().toString();

        // Construir el string de condiciones médicas
        StringBuilder medicalConditions = new StringBuilder();
        if (cbDiabetes.isChecked()) {
            medicalConditions.append("Diabetes");
        }
        if (cbHypertension.isChecked()) {
            if (medicalConditions.length() > 0) medicalConditions.append(", ");
            medicalConditions.append("Hipertensión");
        }

        // --- 3. Crear el objeto User COMPLETO ---
        User newUser = new User();
        newUser.setFullName(name);
        newUser.setEmail(email);
        newUser.setPassword(pass); // En una app real, aquí se debería "hashear" la contraseña
        newUser.setAge(age);
        newUser.setHeight(height);
        newUser.setWeight(weight);
        newUser.setGender(gender);
        newUser.setMedicalConditions(medicalConditions.toString());
        // Dejamos el resto de campos vacíos por ahora, se rellenarán en el perfil
        newUser.setMedications("");
        newUser.setDoctorName("");
        newUser.setEmergencyContact("");

        // --- 4. Intentar registrar en la BD ---
        boolean isRegistered = db.registerUser(newUser);

        if (isRegistered) {
            Toast.makeText(this, "¡Cuenta creada con éxito! Ahora inicia sesión.", Toast.LENGTH_LONG).show();
            finish(); // Cierra la actividad de registro y vuelve al login
        } else {
            Toast.makeText(this, "No se pudo registrar. Es posible que el email ya exista.", Toast.LENGTH_LONG).show();

        }
    }
}
