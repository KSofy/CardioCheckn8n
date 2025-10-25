package com.example.cardiocheck;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cardiocheck.models.User;
import com.example.cardiocheck.utils.SharedPreferencesHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnDoLogin);
        Button btnGoRegister = findViewById(R.id.btnGoRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        btnGoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, com.example.cardiocheck.RegisterActivity.class));
            }
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, R.string.msg_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.msg_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }
        // Eliminamos la validación de la longitud de la contraseña aquí,
        // ya que solo es necesaria en el registro, no en el login.

        User u = db.login(email, pass);
        if (u != null) {
            // Llamamos al método 'login' de SharedPreferencesHelper.
            // Este método ya se encarga de guardar el email, el nombre y de marcar al usuario como logueado.
            SharedPreferencesHelper.login(this, u.getEmail(), u.getFullName());

            Toast.makeText(this, "Bienvenido, " + u.getFullName(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, com.example.cardiocheck.DashboardActivity.class));
            finish();
        } else {
            Toast.makeText(this, R.string.msg_login_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
