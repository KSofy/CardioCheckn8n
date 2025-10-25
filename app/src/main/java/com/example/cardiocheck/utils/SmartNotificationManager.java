package com.example.cardiocheck.utils;

import android.content.Context;
import com.example.cardiocheck.DatabaseHelper;
import com.example.cardiocheck.ReminderReceiver;
import com.example.cardiocheck.models.BloodPressureReading;
import java.util.List;
import java.util.Calendar;

/**
 * Sistema inteligente de análisis de patrones y notificaciones personalizadas
 */
public class SmartNotificationManager {

    private final Context context;
    private final DatabaseHelper db;

    public SmartNotificationManager(Context context) {
        this.context = context;
        this.db = new DatabaseHelper(context);
    }

    /**
     * Analiza patrones y envía notificaciones inteligentes si es necesario
     */
    public void analyzeAndNotify(String userEmail) {
        List<BloodPressureReading> recentReadings = db.getLastReadings(userEmail, 10);

        if (recentReadings.size() < 3) return; // Necesitamos al menos 3 mediciones

        // Análisis de patrones
        PatternAnalysis analysis = analyzePatterns(recentReadings);

        // Verificar si necesita notificación
        if (analysis.needsAttention) {
            sendSmartNotification(analysis);
        }

        // Verificar recordatorios personalizados
        checkPersonalizedReminders(userEmail, recentReadings);
    }

    private PatternAnalysis analyzePatterns(List<BloodPressureReading> readings) {
        PatternAnalysis analysis = new PatternAnalysis();

        // Analizar tendencia de los últimos 5 días
        if (readings.size() >= 5) {
            BloodPressureReading oldest = readings.get(4);
            BloodPressureReading newest = readings.get(0);

            int systolicTrend = newest.getSystolic() - oldest.getSystolic();
            int diastolicTrend = newest.getDiastolic() - oldest.getDiastolic();

            // Tendencia preocupante al alza
            if (systolicTrend > 15 || diastolicTrend > 10) {
                analysis.needsAttention = true;
                analysis.alertType = AlertType.RISING_TREND;
                analysis.message = "Hemos notado que tu presión arterial ha aumentado en los últimos días. Considera consultar con tu médico.";
            }

            // Valores consistentemente altos
            int highReadings = 0;
            for (BloodPressureReading reading : readings.subList(0, Math.min(5, readings.size()))) {
                if (reading.getSystolic() >= 140 || reading.getDiastolic() >= 90) {
                    highReadings++;
                }
            }

            if (highReadings >= 3) {
                analysis.needsAttention = true;
                analysis.alertType = AlertType.CONSISTENTLY_HIGH;
                analysis.message = "Tus últimas mediciones muestran valores elevados de forma consistente. Es recomendable contactar a tu médico.";
            }

            // Variabilidad excesiva
            int maxSystolic = readings.stream().mapToInt(BloodPressureReading::getSystolic).max().orElse(0);
            int minSystolic = readings.stream().mapToInt(BloodPressureReading::getSystolic).min().orElse(0);

            if (maxSystolic - minSystolic > 30) {
                analysis.needsAttention = true;
                analysis.alertType = AlertType.HIGH_VARIABILITY;
                analysis.message = "Tus mediciones muestran gran variabilidad. Esto podría indicar estrés o necesidad de ajustar medicación.";
            }
        }

        return analysis;
    }

    private void checkPersonalizedReminders(String userEmail, List<BloodPressureReading> readings) {
        if (readings.isEmpty()) return;

        // Verificar si hace más de 3 días sin medición
        long lastMeasurement = readings.get(0).getTimestamp();
        long currentTime = System.currentTimeMillis();
        long daysSinceLastMeasurement = (currentTime - lastMeasurement) / (1000 * 60 * 60 * 24);

        if (daysSinceLastMeasurement >= 3) {
            sendReminderNotification(
                "Te extrañamos en CardioCheck",
                "Han pasado " + daysSinceLastMeasurement + " días desde tu última medición. Tu salud cardiovascular es importante."
            );
        }

        // Verificar patrones de horario (recomendar consistencia)
        if (readings.size() >= 7) {
            analyzeTimePatterns(readings);
        }
    }

    private void analyzeTimePatterns(List<BloodPressureReading> readings) {
        // Analizar horarios de medición para sugerir consistencia
        int morningReadings = 0;
        int eveningReadings = 0;

        for (BloodPressureReading reading : readings.subList(0, 7)) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(reading.getTimestamp());
            int hour = cal.get(Calendar.HOUR_OF_DAY);

            if (hour >= 6 && hour <= 10) {
                morningReadings++;
            } else if (hour >= 18 && hour <= 22) {
                eveningReadings++;
            }
        }

        // Si no hay consistencia en horarios, sugerir
        if (morningReadings < 3 && eveningReadings < 3) {
            sendReminderNotification(
                "Consejo de CardioCheck",
                "Para mejores resultados, trata de medir tu presión arterial a la misma hora cada día, preferiblemente por la mañana."
            );
        }
    }

    private void sendSmartNotification(PatternAnalysis analysis) {
        ReminderReceiver.sendCustomNotification(
            context,
            "⚠️ Alerta CardioCheck",
            analysis.message,
            "Toca para ver más detalles",
            true // Prioridad alta
        );
    }

    private void sendReminderNotification(String title, String message) {
        ReminderReceiver.sendCustomNotification(
            context,
            title,
            message,
            "Toca para abrir CardioCheck",
            false // Prioridad normal
        );
    }

    // Clases internas para organizar datos
    private static class PatternAnalysis {
        boolean needsAttention = false;
        AlertType alertType;
        String message;
    }

    private enum AlertType {
        RISING_TREND,
        CONSISTENTLY_HIGH,
        HIGH_VARIABILITY,
        MEDICATION_REMINDER,
        CONSISTENCY_TIP
    }

    /**
     * Método para ser llamado periódicamente (por ejemplo, cada 6 horas)
     */
    public static void performPeriodicAnalysis(Context context, String userEmail) {
        SmartNotificationManager manager = new SmartNotificationManager(context);
        manager.analyzeAndNotify(userEmail);
    }

    /**
     * Configura análisis automático basado en IA
     */
    public void setupIntelligentReminders(String userEmail) {
        List<BloodPressureReading> readings = db.getLastReadings(userEmail, 30);

        if (readings.size() < 5) return;

        // Calcular el mejor horario para recordatorios basado en patrones históricos
        int[] hourlyCount = new int[24];

        for (BloodPressureReading reading : readings) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(reading.getTimestamp());
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            hourlyCount[hour]++;
        }

        // Encontrar la hora más común
        int bestHour = 9; // Default
        int maxCount = 0;

        for (int i = 6; i <= 22; i++) { // Solo horarios razonables
            if (hourlyCount[i] > maxCount) {
                maxCount = hourlyCount[i];
                bestHour = i;
            }
        }

        // Configurar recordatorio inteligente
        if (maxCount >= 3) { // Si hay un patrón claro
            SharedPreferencesHelper.setReminderTime(context, bestHour, 0);

            sendReminderNotification(
                "🧠 CardioCheck Inteligente",
                "He notado que sueles medirte a las " + bestHour + ":00. ¿Te parece un buen horario para recordatorios?"
            );
        }
    }
}
