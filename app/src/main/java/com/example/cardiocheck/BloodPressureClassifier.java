package com.example.cardiocheck;

import android.content.Context;
import androidx.core.content.ContextCompat;

import com.example.cardiocheck.models.BloodPressureReading;

public class BloodPressureClassifier {

    // Contenedor simple para el resultado de la clasificación
    public static class ClassificationResult {
        public final String category;
        public final int color;

        public ClassificationResult(String category, int color) {
            this.category = category;
            this.color = color;
        }
    }

    /**
     * Clasifica una lectura de presión arterial según las guías estándar.
     * @param context El contexto para acceder a los colores.
     * @param reading La medición a clasificar.
     * @return Un objeto ClassificationResult con la categoría y el color.
     */
    public static ClassificationResult classify(Context context, BloodPressureReading reading) {
        int systolic = reading.getSystolic();
        int diastolic = reading.getDiastolic();

        if (systolic < 120 && diastolic < 80) {
            return new ClassificationResult("Óptima", ContextCompat.getColor(context, R.color.bp_optimal));
        } else if (systolic >= 120 && systolic <= 129 && diastolic < 80) {
            return new ClassificationResult("Elevada", ContextCompat.getColor(context, R.color.bp_elevated));
        } else if ((systolic >= 130 && systolic <= 139) || (diastolic >= 80 && diastolic <= 89)) {
            return new ClassificationResult("Hipertensión (Etapa 1)", ContextCompat.getColor(context, R.color.bp_stage1));
        } else if (systolic >= 140 || diastolic >= 90) {
            return new ClassificationResult("Hipertensión (Etapa 2)", ContextCompat.getColor(context, R.color.bp_stage2));
        } else if (systolic > 180 || diastolic > 120) {
            return new ClassificationResult("Crisis Hipertensiva", ContextCompat.getColor(context, R.color.bp_crisis));
        } else {
            return new ClassificationResult("Normal", ContextCompat.getColor(context, R.color.bp_optimal)); // Caso por defecto
        }
    }
}