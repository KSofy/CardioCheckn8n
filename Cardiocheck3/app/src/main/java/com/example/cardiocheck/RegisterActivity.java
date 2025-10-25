package com.example.cardiocheck;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cardiocheck.models.User;

/**
 * Pantalla de registro de usuario.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = new DatabaseHelper(this);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnRegister = findViewById(R.id.btnDoRegister);
        Button btnGoToLogin = findViewById(R.id.btnGoToLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ir a la pantalla de inicio de sesión
                finish();
            }
        });
    }

    private void register() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, R.string.msg_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.msg_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 8) {
            Toast.makeText(this, R.string.msg_invalid_password, Toast.LENGTH_SHORT).show();
            return;
        }

        User u = new User(0, name, email, pass);
        boolean ok = db.registerUser(u);
        if (ok) {
            Toast.makeText(this, "Cuenta creada. Ahora inicia sesión.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "No se pudo registrar. ¿Email ya registrado?", Toast.LENGTH_SHORT).show();
        }
    }
}
