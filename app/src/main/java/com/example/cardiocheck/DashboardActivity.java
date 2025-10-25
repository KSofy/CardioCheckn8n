package com.example.cardiocheck;import android.app.AlertDialog; import android.content.Intent; import android.os.Bundle; import android.text.TextUtils; import android.util.Log; import android.view.View; import android.widget.Button; import android.widget.ImageButton; import android.widget.LinearLayout; import android.widget.TextView; import android.widget.Toast;import androidx.appcompat.app.AppCompatActivity;import com.android.volley.Request; import com.android.volley.toolbox.JsonObjectRequest; import com.android.volley.toolbox.Volley; import com.example.cardiocheck.models.BloodPressureReading; import com.example.cardiocheck.models.User; import com.example.cardiocheck.utils.ChartHelper; import com.example.cardiocheck.utils.SharedPreferencesHelper; import com.github.mikephil.charting.charts.LineChart; import com.google.android.material.textfield.TextInputEditText;import org.json.JSONException; import org.json.JSONObject;import java.text.SimpleDateFormat; import java.util.Date; import java.util.List; import java.util.Locale;public class DashboardActivity extends AppCompatActivity {private DatabaseHelper db;
    private OpenAIClient aiClient;
    private TextView tvAnalysis;
    private TextView tvUserName;
    private TextView tvLastSystolic, tvLastDiastolic, tvLastPulse;
    private TextView tvLastReadingTime, tvStatusBadge;
    private TextView tvWeeklyAvg, tvWeeklyStatus, tvPulseAvg;
    private TextView btnWeek, btnMonth, btnQuarter;
    private ImageButton btnProfile, btnLogout;
    private LineChart chartBloodPressure;
    private String selectedPeriod = "week";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (!SharedPreferencesHelper.isLoggedIn(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupEventListeners();

        db = new DatabaseHelper(this);
        aiClient = new OpenAIClient(this);

        loadUserInfo();
        refreshDashboard();
    }

    private void initializeViews() {
        tvUserName = findViewById(R.id.tvUserName);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        tvLastSystolic = findViewById(R.id.tvLastSystolic);
        tvLastDiastolic = findViewById(R.id.tvLastDiastolic);
        tvLastPulse = findViewById(R.id.tvLastPulse);
        tvLastReadingTime = findViewById(R.id.tvLastReadingTime);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvWeeklyAvg = findViewById(R.id.tvWeeklyAvg);
        tvWeeklyStatus = findViewById(R.id.tvWeeklyStatus);
        tvPulseAvg = findViewById(R.id.tvPulseAvg);
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);
        btnQuarter = findViewById(R.id.btnQuarter);
        chartBloodPressure = findViewById(R.id.chartBloodPressure);
        tvAnalysis = findViewById(R.id.tvAnalysis);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnAI = findViewById(R.id.btnAI);
        View fabAdd = findViewById(R.id.fabAdd);
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnAI.setOnClickListener(v -> startActivity(new Intent(this, AIChatActivity.class)));
        fabAdd.setOnClickListener(v -> showAddReadingDialog());
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void setupEventListeners() {
        // Abrir la pantalla de edición de perfil
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            SharedPreferencesHelper.logout(this);
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        btnWeek.setOnClickListener(v -> selectPeriod("week", btnWeek));
        btnMonth.setOnClickListener(v -> selectPeriod("month", btnMonth));
        btnQuarter.setOnClickListener(v -> selectPeriod("quarter", btnQuarter));
    }

    // --- MÉTODO COMPLETAMENTE ACTUALIZADO ---
    private void showAddReadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_reading, null);
        builder.setView(view);

        // Vistas de la Sección 1: Entrada
        LinearLayout inputLayout = view.findViewById(R.id.inputLayout);
        TextInputEditText etSystolic = view.findViewById(R.id.etSystolic);
        TextInputEditText etDiastolic = view.findViewById(R.id.etDiastolic);
        TextInputEditText etPulse = view.findViewById(R.id.etPulse);
        Button btnSaveReading = view.findViewById(R.id.btnSaveReading);

        // Vistas de la Sección 2: Resumen
        LinearLayout summaryLayout = view.findViewById(R.id.summaryLayout);
        TextView tvResultValue = view.findViewById(R.id.tvResultValue);
        TextView tvResultPulse = view.findViewById(R.id.tvResultPulse);
        TextView tvClassification = view.findViewById(R.id.tvClassification);
        Button btnAnalyzeNow = view.findViewById(R.id.btnAnalyzeNow);
        Button btnCloseDialog = view.findViewById(R.id.btnCloseDialog);

        // Vistas de la NUEVA Sección 3: Resultado de IA
        LinearLayout analysisResultLayout = view.findViewById(R.id.analysisResultLayout);
        TextView tvAIResult = view.findViewById(R.id.tvAIResult);
        Button btnFinishDialog = view.findViewById(R.id.btnFinishDialog);

        AlertDialog dialog = builder.create();

        btnSaveReading.setOnClickListener(v -> {
            String sysStr = etSystolic.getText().toString().trim();
            String diaStr = etDiastolic.getText().toString().trim();
            String pulStr = etPulse.getText().toString().trim();

            if (sysStr.isEmpty() || diaStr.isEmpty() || pulStr.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int systolic = Integer.parseInt(sysStr);
                int diastolic = Integer.parseInt(diaStr);
                int pulse = Integer.parseInt(pulStr);

                if (systolic < 70 || systolic > 250 || diastolic < 40 || diastolic > 130 || pulse < 40 || pulse > 200) {
                    Toast.makeText(this, "Valores fuera de los rangos válidos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                long timestamp = System.currentTimeMillis();
                String userEmail = SharedPreferencesHelper.getUserEmail(this);

                BloodPressureReading newReading = new BloodPressureReading();
                newReading.setEmail(userEmail);
                newReading.setSystolic(systolic);
                newReading.setDiastolic(diastolic);
                newReading.setPulse(pulse);
                newReading.setTimestamp(timestamp);
                newReading.setAiRecommendation("");

                long id = db.insertReading(newReading);

                if (id == -1) {
                    Toast.makeText(this, "No se pudo guardar la medición.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, "Medición guardada con éxito", Toast.LENGTH_SHORT).show();
                newReading.setId(id);
                sendDataToN8n(newReading);

                inputLayout.setVisibility(View.GONE);
                summaryLayout.setVisibility(View.VISIBLE);

                tvResultValue.setText(String.format(Locale.getDefault(), "%d/%d mmHg", systolic, diastolic));
                tvResultPulse.setText(String.format(Locale.getDefault(), "%d bpm", pulse));

                BloodPressureClassifier.ClassificationResult result = BloodPressureClassifier.classify(this, newReading);
                tvClassification.setText(result.category);
                tvClassification.setBackgroundColor(result.color);

                btnAnalyzeNow.setOnClickListener(an -> {
                    btnAnalyzeNow.setEnabled(false);
                    btnAnalyzeNow.setText("Analizando...");

                    new Thread(() -> {
                        User currentUser = db.getUserByEmail(userEmail);
                        String advice = aiClient.getAdviceForSingleReading(newReading, currentUser);
                        db.updateReadingRecommendation(newReading.getId(), advice);

                        runOnUiThread(() -> {
                            // --- LÓGICA CORREGIDA ---
                            // 1. Ocultamos la sección de resumen
                            summaryLayout.setVisibility(View.GONE);
                            // 2. Mostramos la nueva sección con el resultado
                            analysisResultLayout.setVisibility(View.VISIBLE);
                            // 3. Ponemos el consejo de la IA en el TextView del diálogo
                            tvAIResult.setText(advice);
                        });
                    }).start();
                });

            } catch (Exception e) {
                Log.e("AddReadingError", "Error al guardar la medición", e);
                Toast.makeText(this, "Error en los datos introducidos.", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón para cerrar el diálogo si NO se pide análisis
        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        // Botón para finalizar y cerrar el diálogo DESPUÉS de ver el análisis
        btnFinishDialog.setOnClickListener(v -> {
            refreshDashboard(); // Ahora sí refrescamos la pantalla principal
            dialog.dismiss();
        });

        dialog.show();
    }

    private void sendDataToN8n(BloodPressureReading reading) {
        String webhookUrl = "https://primary-production-7bc2e.up.railway.app/webhook/6b819410-23ab-4ee0-8e4b-2bdb3f2ab28a";
        String userEmail = SharedPreferencesHelper.getUserEmail(this);
        User currentUser = db.getUserByEmail(userEmail);

        if (currentUser == null) {
            Log.e("N8N_ERROR", "No se pudo encontrar al usuario para enviar datos.");
            return;
        }

        JSONObject postData = new JSONObject();
        try {
            postData.put("systolic", reading.getSystolic());
            postData.put("diastolic", reading.getDiastolic());
            postData.put("pulse", reading.getPulse());
            postData.put("userEmail", userEmail);
            postData.put("userName", currentUser.getFullName());
            postData.put("emergencyContact", currentUser.getEmergencyContact());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                webhookUrl,
                postData,
                response -> Log.d("N8N_SUCCESS", "Datos enviados a n8n con éxito."),
                error -> Log.e("N8N_ERROR", "Error al enviar datos a n8n: " + error.toString())
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void loadUserInfo() {
        String fullName = SharedPreferencesHelper.getUserFullName(this);
        if (!TextUtils.isEmpty(fullName)) {
            tvUserName.setText(fullName);
        } else {
            tvUserName.setText("Usuario");
        }
    }

    private void refreshDashboard() {
        String email = SharedPreferencesHelper.getUserEmail(this);
        List<BloodPressureReading> readings = db.getLastReadings(email, 30);
        if (readings == null || readings.isEmpty()) {
            showEmptyState();
            return;
        }
        updateLastReading(readings.get(0));
        updateStatistics(readings);
        updateAnalysis(readings);
        updateChartPlaceholder(selectedPeriod);
    }

    private void showEmptyState() {
        tvLastSystolic.setText("--");
        tvLastDiastolic.setText("--");
        tvLastPulse.setText("-- BPM");
        tvLastReadingTime.setText("Sin mediciones");
        tvStatusBadge.setText("Sin datos");
        tvStatusBadge.setBackgroundResource(R.drawable.status_badge_normal);
        tvWeeklyAvg.setText("--/--");
        tvWeeklyStatus.setText("Sin datos");
        tvPulseAvg.setText("--");
        tvAnalysis.setText("Aún no hay mediciones. Usa el botón '+' para registrar la primera.");
        chartBloodPressure.clear();
        chartBloodPressure.invalidate();
    }

    private void updateLastReading(BloodPressureReading reading) {
        tvLastSystolic.setText(String.valueOf(reading.getSystolic()));
        tvLastDiastolic.setText(String.valueOf(reading.getDiastolic()));
        tvLastPulse.setText(String.format(Locale.getDefault(), "%d BPM", reading.getPulse()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM, HH:mm", Locale.getDefault());
        tvLastReadingTime.setText(sdf.format(new Date(reading.getTimestamp())));
        updateStatusBadge(reading.getSystolic(), reading.getDiastolic());
    }

    private void updateStatusBadge(int systolic, int diastolic) {
        BloodPressureReading tempReading = new BloodPressureReading();
        tempReading.setSystolic(systolic);
        tempReading.setDiastolic(diastolic);
        BloodPressureClassifier.ClassificationResult result = BloodPressureClassifier.classify(this, tempReading);
        tvStatusBadge.setText(result.category);
        tvStatusBadge.setBackgroundResource(R.drawable.status_badge_normal);
    }

    private void updateStatistics(List<BloodPressureReading> readings) {
        if (readings.size() < 2) {
            tvWeeklyAvg.setText("--/--");
            tvWeeklyStatus.setText("Pocos datos");
            tvPulseAvg.setText("--");
            return;
        }
        int count = Math.min(7, readings.size());
        int totalSys = 0, totalDia = 0, totalPul = 0;
        for (int i = 0; i < count; i++) {
            totalSys += readings.get(i).getSystolic();
            totalDia += readings.get(i).getDiastolic();
            totalPul += readings.get(i).getPulse();
        }
        int avgSys = totalSys / count;
        int avgDia = totalDia / count;
        tvWeeklyAvg.setText(String.format(Locale.getDefault(), "%d/%d", avgSys, avgDia));
        tvWeeklyStatus.setText(String.format(Locale.getDefault(), "%d mediciones", count));
        tvPulseAvg.setText(String.valueOf(totalPul / count));
        updateWeeklyStatusColor(avgSys, avgDia);
    }

    private void updateWeeklyStatusColor(int systolic, int diastolic) {
        BloodPressureReading tempReading = new BloodPressureReading();
        tempReading.setSystolic(systolic);
        tempReading.setDiastolic(diastolic);
        BloodPressureClassifier.ClassificationResult result = BloodPressureClassifier.classify(this, tempReading);
        tvWeeklyStatus.setTextColor(result.color);
    }

    private void selectPeriod(String period, TextView selectedButton) {
        resetPeriodButtons();
        selectedButton.setBackgroundResource(R.drawable.period_button_selected);
        selectedButton.setTextColor(getResources().getColor(R.color.text_white, getTheme()));
        selectedPeriod = period;
        updateChartPlaceholder(period);
    }

    private void resetPeriodButtons() {
        TextView[] buttons = {btnWeek, btnMonth, btnQuarter};
        for (TextView button : buttons) {
            button.setBackground(null);
            button.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        }
    }

    private void updateChartPlaceholder(String period) {
        String email = SharedPreferencesHelper.getUserEmail(this);
        int limit = period.equals("month") ? 30 : (period.equals("quarter") ? 90 : 7);
        List<BloodPressureReading> readings = db.getLastReadings(email, limit);
        ChartHelper.setupBloodPressureChart(this, chartBloodPressure, readings);
    }

    private void updateAnalysis(List<BloodPressureReading> readings) {
        if (readings.isEmpty()) return;
        BloodPressureReading latest = readings.get(0);
        String rec = latest.getAiRecommendation();
        if (rec != null && !isInvalidAIMessage(rec)) {
            tvAnalysis.setText(rec);
        } else {
            tvAnalysis.setText("Registra una nueva medición para obtener un análisis actualizado.");
        }
    }

    private boolean isInvalidAIMessage(String rec) {
        if (TextUtils.isEmpty(rec)) return true;
        String lower = rec.toLowerCase();
        return lower.contains("clave de openai") || lower.contains("no hay una clave");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
        refreshDashboard();
    }}