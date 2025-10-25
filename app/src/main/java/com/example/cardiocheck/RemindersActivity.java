package com.example.cardiocheck;

import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cardiocheck.utils.SharedPreferencesHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Actividad para configurar recordatorios de mediciones
 */
public class RemindersActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    private SwitchMaterial switchReminder;
    private LinearLayout layoutTimeConfig;
    private TextView tvSelectedTime;
    private Button btnSelectTime, btnTestNotification;
    private ImageButton btnBack;

    private int selectedHour = 9;
    private int selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        initializeViews();
        loadCurrentSettings();
        setupEventListeners();
        checkNotificationPermission();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        switchReminder = findViewById(R.id.switchReminder);
        layoutTimeConfig = findViewById(R.id.layoutTimeConfig);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnTestNotification = findViewById(R.id.btnTestNotification);
    }

    private void loadCurrentSettings() {
        // Cargar configuración actual
        boolean isEnabled = SharedPreferencesHelper.isReminderEnabled(this);
        selectedHour = SharedPreferencesHelper.getReminderHour(this);
        selectedMinute = SharedPreferencesHelper.getReminderMinute(this);

        switchReminder.setChecked(isEnabled);
        updateTimeDisplay();
        updateTimeConfigVisibility(isEnabled);
    }

    private void setupEventListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTimeConfigVisibility(isChecked);

            if (isChecked) {
                if (hasNotificationPermission()) {
                    enableReminders();
                } else {
                    requestNotificationPermission();
                    switchReminder.setChecked(false);
                }
            } else {
                disableReminders();
            }
        });

        btnSelectTime.setOnClickListener(v -> showTimePicker());

        btnTestNotification.setOnClickListener(v -> {
            if (hasNotificationPermission()) {
                showTestNotification();
            } else {
                requestNotificationPermission();
            }
        });
    }

    private void updateTimeConfigVisibility(boolean show) {
        layoutTimeConfig.setVisibility(show ? LinearLayout.VISIBLE : LinearLayout.GONE);
    }

    private void updateTimeDisplay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        tvSelectedTime.setText(timeFormat.format(calendar.getTime()));
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                updateTimeDisplay();

                // Si los recordatorios están habilitados, actualizar la programación
                if (switchReminder.isChecked()) {
                    enableReminders();
                }
            },
            selectedHour,
            selectedMinute,
            false // Usar formato 12 horas
        );

        timePickerDialog.setTitle("Seleccionar hora del recordatorio");
        timePickerDialog.show();
    }

    private void enableReminders() {
        ReminderReceiver.scheduleReminder(this, selectedHour, selectedMinute);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);

        String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.getTime());
        Toast.makeText(this, "Recordatorio programado para las " + timeStr, Toast.LENGTH_LONG).show();
    }

    private void disableReminders() {
        ReminderReceiver.cancelReminder(this);
        Toast.makeText(this, "Recordatorios desactivados", Toast.LENGTH_SHORT).show();
    }

    private void showTestNotification() {
        // Crear una instancia temporal del receiver para probar
        ReminderReceiver receiver = new ReminderReceiver();
        receiver.onReceive(this, null);
        Toast.makeText(this, "Notificación de prueba enviada", Toast.LENGTH_SHORT).show();
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                   == PackageManager.PERMISSION_GRANTED;
        }
        return true; // En versiones anteriores no se necesita permiso explícito
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                // Si no tiene permisos y los recordatorios están habilitados, deshabilitarlos
                if (SharedPreferencesHelper.isReminderEnabled(this)) {
                    SharedPreferencesHelper.setReminderEnabled(this, false);
                    switchReminder.setChecked(false);
                    updateTimeConfigVisibility(false);
                }
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(this,
                    "CardioCheck necesita permisos de notificación para enviarte recordatorios útiles",
                    Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, habilitar recordatorios
                switchReminder.setChecked(true);
                updateTimeConfigVisibility(true);
                enableReminders();
                Toast.makeText(this, "¡Perfecto! Ya puedes recibir recordatorios", Toast.LENGTH_SHORT).show();
            } else {
                // Permiso denegado
                Toast.makeText(this,
                    "Sin permisos de notificación no podemos enviarte recordatorios",
                    Toast.LENGTH_LONG).show();
                switchReminder.setChecked(false);
                updateTimeConfigVisibility(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNotificationPermission();
    }
}
