package com.example.cardiocheck;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cardiocheck.models.BloodPressureReading;
import com.example.cardiocheck.utils.SharedPreferencesHelper;

import java.util.List;

/**
 * Pantalla principal: muestra análisis de hoy, botones de navegación y FAB para registrar medición.
 */
public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private OpenAIClient aiClient;
    private TextView tvAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (!SharedPreferencesHelper.isLoggedIn(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        db = new DatabaseHelper(this);
        // Limpiar mensajes antiguos que pedían configurar la clave de IA
        db.clearInvalidAIRecommendations();
        aiClient = new OpenAIClient(this);
        tvAnalysis = findViewById(R.id.tvAnalysis);
        Button btnAI = findViewById(R.id.btnAI);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnLogout = findViewById(R.id.btnLogout);
        View fab = findViewById(R.id.fabAdd);

        btnAI.setOnClickListener(v -> startActivity(new Intent(this, AIChatActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnLogout.setOnClickListener(v -> {
            com.example.cardiocheck.utils.SharedPreferencesHelper.logout(this);
            Intent i = new Intent(this, com.example.cardiocheck.MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        fab.setOnClickListener(v -> showAddDialog());

        refreshAnalysis();
    }

    private void refreshAnalysis() {
        String email = SharedPreferencesHelper.getUserEmail(this);
        List<BloodPressureReading> last = db.getLastReadings(email, 3);
        if (last.isEmpty()) {
            tvAnalysis.setText("Aún no hay mediciones. Usa el botón para registrar la primera.");
            return;
        }
        BloodPressureReading latest = last.get(0);
        String rec = latest.getAiRecommendation();
        if (!isInvalidAIMessage(rec)) {
            tvAnalysis.setText(rec);
        } else {
            // Recalcular con la clave ya integrada y actualizar BD
            new Thread(() -> {
                final String r = aiClient.getAnalysisRecommendation(last);
                db.updateAIRecommendation(latest.getId(), r);
                runOnUiThread(() -> tvAnalysis.setText(r));
            }).start();
        }
    }

    private boolean isInvalidAIMessage(String rec) {
        if (TextUtils.isEmpty(rec)) return true;
        String lower = rec.toLowerCase();
        return lower.contains("clave de openai") ||
                lower.contains("configura tu clave") ||
                lower.contains("no hay una clave de openai") ||
                lower.contains("ingresa tu clave de openai");
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_reading, null);
        EditText etSys = view.findViewById(R.id.etSystolic);
        EditText etDia = view.findViewById(R.id.etDiastolic);
        EditText etPulse = view.findViewById(R.id.etPulse);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Nueva medición")
                .setView(view)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(di -> {
            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(v -> {
                try {
                    int s = Integer.parseInt(etSys.getText().toString().trim());
                    int diastolicVal = Integer.parseInt(etDia.getText().toString().trim());
                    int p = Integer.parseInt(etPulse.getText().toString().trim());
                    if (s < 70 || s > 250 || diastolicVal < 40 || diastolicVal > 130 || p < 40 || p > 200) {
                        Toast.makeText(this, R.string.msg_invalid_ranges, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String email = SharedPreferencesHelper.getUserEmail(this);
                    long ts = System.currentTimeMillis();
                    BloodPressureReading r = new BloodPressureReading(0, email, s, diastolicVal, p, ts, "");
                    long id = db.insertReading(r);

                    if (id == -1) {
                        Toast.makeText(this, "No se pudo guardar.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        List<BloodPressureReading> last = db.getLastReadings(email, 3);
                        String rec = aiClient.getAnalysisRecommendation(last);
                        db.updateAIRecommendation(id, rec);
                        runOnUiThread(() -> {
                            Toast.makeText(this, getString(R.string.msg_saved), Toast.LENGTH_SHORT).show();
                            tvAnalysis.setText(rec);
                        });
                    }).start();

                    dialog.dismiss();
                } catch (Exception ex) {
                    Toast.makeText(this, R.string.msg_required_fields, Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }
}
