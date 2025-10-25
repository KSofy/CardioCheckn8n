package com.example.cardiocheck;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardiocheck.models.BloodPressureReading;
import com.example.cardiocheck.models.User; // <- IMPORTACIÓN AÑADIDA
import com.example.cardiocheck.utils.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements ReadingAdapter.OnSelectionChangedListener {

    private ReadingAdapter adapter;
    private DatabaseHelper db;
    private MenuItem actionGenerate;
    private AlertDialog progressDialog; // Usaremos un AlertDialog para el progreso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = new DatabaseHelper(this);
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String email = SharedPreferencesHelper.getUserEmail(this);
        List<BloodPressureReading> all = db.getAllReadings(email);
        adapter = new ReadingAdapter(all, this);
        recyclerView.setAdapter(adapter);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_history);
        actionGenerate = toolbar.getMenu().findItem(R.id.action_generate_pdf);
        if (actionGenerate != null) actionGenerate.setEnabled(false);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_generate_pdf) {
                generateAndSendPdf();
                return true;
            } else if (item.getItemId() == R.id.action_ai_advice) {
                requestAIAdvice();
                return true;
            }
            return false;
        });
    }

    /**
     * MÉTODO MODIFICADO: Ahora obtiene el perfil del usuario y lo envía a la IA.
     * Muestra un diálogo de carga mientras espera la respuesta.
     */
    private void requestAIAdvice() {
        final List<BloodPressureReading> selectedReadings = new ArrayList<>(adapter.getSelected());
        if (selectedReadings.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una medición para el análisis.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar un indicador de carga
        showLoading(true, "Analizando con IA...");

        new Thread(() -> {
            // 1. Obtener el email del usuario logueado
            String userEmail = SharedPreferencesHelper.getUserEmail(this);
            if (userEmail == null) {
                runOnUiThread(() -> {
                    showLoading(false, null);
                    Toast.makeText(this, "Error de autenticación. No se pudo encontrar el usuario.", Toast.LENGTH_LONG).show();
                });
                return;
            }

            // 2. Obtener el perfil completo del usuario desde la BD
            User currentUser = db.getUserByEmail(userEmail);

            // 3. Llamar al cliente de IA con las mediciones Y el perfil
            OpenAIClient client = new OpenAIClient(this);
            final String advice = client.getAdviceForSelected(selectedReadings, currentUser);

            // 4. Mostrar el resultado en la UI
            runOnUiThread(() -> {
                showLoading(false, null);
                new AlertDialog.Builder(this)
                        .setTitle("Consejo de Cardio-IA")
                        .setMessage(advice)
                        .setPositiveButton("Entendido", null)
                        .show();
            });
        }).start();
    }

    private void showLoading(boolean isLoading, String message) {
        if (isLoading) {
            if (progressDialog == null) {
                progressDialog = new AlertDialog.Builder(this)
                        .setTitle(message)
                        .setView(R.layout.dialog_progress) // Asume que tienes un layout simple con un ProgressBar
                        .setCancelable(false)
                        .create();
            }
            progressDialog.show();
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void generateAndSendPdf() {
        List<BloodPressureReading> selected = new ArrayList<>(adapter.getSelected());
        if (selected.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una medición", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            PDFGenerator generator = new PDFGenerator(this);
            File file = generator.generatePDF(selected, SharedPreferencesHelper.getUserFullName(this));

            runOnUiThread(() -> {
                if (file == null) {
                    Toast.makeText(this, "No se pudo generar el PDF.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, R.string.msg_pdf_generated, Toast.LENGTH_SHORT).show();
                sharePdf(file);
            });
        }).start();
    }

    private void sharePdf(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this, "com.example.cardiocheck.fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Cardio Check – Reporte");
            intent.putExtra(Intent.EXTRA_TEXT, "Adjunto tu reporte de presión arterial.");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Enviar PDF"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.msg_no_email_app, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSelectionChanged(int count) {
        if (actionGenerate != null) actionGenerate.setEnabled(count > 0);
    }
}

