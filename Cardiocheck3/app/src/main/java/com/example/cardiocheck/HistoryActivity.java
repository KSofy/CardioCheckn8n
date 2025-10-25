package com.example.cardiocheck;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardiocheck.models.BloodPressureReading;
import com.example.cardiocheck.utils.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements ReadingAdapter.OnSelectionChangedListener {

    private ReadingAdapter adapter;
    private DatabaseHelper db;
    private MenuItem actionGenerate;

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
            } else if (item.getItemId() == R.id.action_return_main_menu) {
                Intent intent = new Intent(HistoryActivity.this, DashboardActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void requestAIAdvice() {
        final List<BloodPressureReading> selected = new ArrayList<>(adapter.getSelected());
        if (selected.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una medición", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            OpenAIClient client = new OpenAIClient(this);
            final String advice = client.getAdviceForSelected(selected);
            runOnUiThread(() -> new AlertDialog.Builder(this)
                    .setTitle("Consejo de IA")
                    .setMessage(advice)
                    .setPositiveButton(android.R.string.ok, null)
                    .show());
        }).start();
    }

    private void generateAndSendPdf() {
        List<BloodPressureReading> selected = new ArrayList<>(adapter.getSelected());
        if (selected.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una medición", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            PDFGenerator generator = new PDFGenerator(this);
            File file = generator.generatePDF(selected, SharedPreferencesHelper.getUserName(this));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pdf, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_return_main_menu) {
            Intent intent = new Intent(HistoryActivity.this, DashboardActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectionChanged(int count) {
        if (actionGenerate != null) actionGenerate.setEnabled(count > 0);
    }
}
