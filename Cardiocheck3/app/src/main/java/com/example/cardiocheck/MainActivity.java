package com.example.cardiocheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cardiocheck.utils.SharedPreferencesHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Mostrar/ocultar botón de cerrar sesión según estado actual
        boolean loggedIn = SharedPreferencesHelper.isLoggedIn(this);
        btnLogout.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

        btnLogout.setOnClickListener(v -> {
            SharedPreferencesHelper.logout(this);
            btnLogout.setVisibility(View.GONE);
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_help) {
            // Aquí puedes mostrar un diálogo de ayuda o abrir una nueva Activity
            return true;
        } else if (id == R.id.action_exit) {
            finishAffinity(); // Cierra la app
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}