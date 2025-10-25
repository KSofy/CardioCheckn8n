package com.example.cardiocheck.utils;

import android.content.Context;
import android.graphics.Color;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.example.cardiocheck.R;
import com.example.cardiocheck.models.BloodPressureReading;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Clase helper para configurar y generar gráficas de presión arterial
 */
public class ChartHelper {

    public static void setupBloodPressureChart(Context context, LineChart chart, List<BloodPressureReading> readings) {
        if (readings == null || readings.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        // Configurar descripción
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);

        // Configurar interacción
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        // Crear entradas de datos
        ArrayList<Entry> systolicEntries = new ArrayList<>();
        ArrayList<Entry> diastolicEntries = new ArrayList<>();

        // Invertir la lista para mostrar fechas más recientes a la derecha
        List<BloodPressureReading> reversedReadings = new ArrayList<>(readings);
        java.util.Collections.reverse(reversedReadings);

        for (int i = 0; i < reversedReadings.size(); i++) {
            BloodPressureReading reading = reversedReadings.get(i);
            systolicEntries.add(new Entry(i, reading.getSystolic()));
            diastolicEntries.add(new Entry(i, reading.getDiastolic()));
        }

        // Configurar dataset sistólica
        LineDataSet systolicDataSet = new LineDataSet(systolicEntries, "Sistólica");
        systolicDataSet.setColor(context.getResources().getColor(R.color.bp_stage1, null));
        systolicDataSet.setCircleColor(context.getResources().getColor(R.color.bp_stage1, null));
        systolicDataSet.setLineWidth(3f);
        systolicDataSet.setCircleRadius(4f);
        systolicDataSet.setDrawCircleHole(false);
        systolicDataSet.setValueTextSize(0f); // Ocultar valores en puntos
        systolicDataSet.setDrawFilled(false);

        // Configurar dataset diastólica
        LineDataSet diastolicDataSet = new LineDataSet(diastolicEntries, "Diastólica");
        diastolicDataSet.setColor(context.getResources().getColor(R.color.primary_blue, null));
        diastolicDataSet.setCircleColor(context.getResources().getColor(R.color.primary_blue, null));
        diastolicDataSet.setLineWidth(3f);
        diastolicDataSet.setCircleRadius(4f);
        diastolicDataSet.setDrawCircleHole(false);
        diastolicDataSet.setValueTextSize(0f); // Ocultar valores en puntos
        diastolicDataSet.setDrawFilled(false);

        // Crear LineData
        LineData lineData = new LineData(systolicDataSet, diastolicDataSet);
        chart.setData(lineData);

        // Configurar eje X (fechas)
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(5, readings.size()));
        xAxis.setTextColor(context.getResources().getColor(R.color.text_secondary, null));
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < reversedReadings.size()) {
                    BloodPressureReading reading = reversedReadings.get(index);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    return sdf.format(new Date(reading.getTimestamp()));
                }
                return "";
            }
        });

        // Configurar eje Y izquierdo
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(context.getResources().getColor(R.color.divider, null));
        leftAxis.setTextColor(context.getResources().getColor(R.color.text_secondary, null));
        leftAxis.setAxisMinimum(50f);
        leftAxis.setAxisMaximum(200f);

        // Agregar líneas de referencia para límites normales
        leftAxis.addLimitLine(createLimitLine(context, 120f, "Normal Sistólica", R.color.bp_normal));
        leftAxis.addLimitLine(createLimitLine(context, 80f, "Normal Diastólica", R.color.bp_optimal));
        leftAxis.addLimitLine(createLimitLine(context, 140f, "Hipertensión", R.color.bp_stage2));

        // Deshabilitar eje Y derecho
        chart.getAxisRight().setEnabled(false);

        // Configurar leyenda
        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextColor(context.getResources().getColor(R.color.text_primary, null));

        // Refrescar gráfica
        chart.invalidate();
    }

    private static com.github.mikephil.charting.components.LimitLine createLimitLine(Context context, float value, String label, int colorRes) {
        com.github.mikephil.charting.components.LimitLine limitLine =
            new com.github.mikephil.charting.components.LimitLine(value, label);
        limitLine.setLineColor(context.getResources().getColor(colorRes, null));
        limitLine.setLineWidth(1f);
        limitLine.setTextColor(context.getResources().getColor(colorRes, null));
        limitLine.setTextSize(10f);
        limitLine.enableDashedLine(10f, 5f, 0f);
        return limitLine;
    }

    public static void setupPulseChart(Context context, LineChart chart, List<BloodPressureReading> readings) {
        if (readings == null || readings.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        // Configurar descripción
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);

        // Configurar interacción
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        // Crear entradas de datos para pulso
        ArrayList<Entry> pulseEntries = new ArrayList<>();

        List<BloodPressureReading> reversedReadings = new ArrayList<>(readings);
        java.util.Collections.reverse(reversedReadings);

        for (int i = 0; i < reversedReadings.size(); i++) {
            BloodPressureReading reading = reversedReadings.get(i);
            pulseEntries.add(new Entry(i, reading.getPulse()));
        }

        // Configurar dataset pulso
        LineDataSet pulseDataSet = new LineDataSet(pulseEntries, "Pulso (BPM)");
        pulseDataSet.setColor(context.getResources().getColor(R.color.accent_teal, null));
        pulseDataSet.setCircleColor(context.getResources().getColor(R.color.accent_teal, null));
        pulseDataSet.setLineWidth(3f);
        pulseDataSet.setCircleRadius(4f);
        pulseDataSet.setDrawCircleHole(false);
        pulseDataSet.setValueTextSize(0f);
        pulseDataSet.setDrawFilled(true);
        pulseDataSet.setFillColor(context.getResources().getColor(R.color.accent_teal, null));
        pulseDataSet.setFillAlpha(30);

        // Crear LineData
        LineData lineData = new LineData(pulseDataSet);
        chart.setData(lineData);

        // Configurar ejes similar al anterior pero con rangos para pulso
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(Math.min(5, readings.size()));
        xAxis.setTextColor(context.getResources().getColor(R.color.text_secondary, null));
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < reversedReadings.size()) {
                    BloodPressureReading reading = reversedReadings.get(index);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    return sdf.format(new Date(reading.getTimestamp()));
                }
                return "";
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(context.getResources().getColor(R.color.divider, null));
        leftAxis.setTextColor(context.getResources().getColor(R.color.text_secondary, null));
        leftAxis.setAxisMinimum(40f);
        leftAxis.setAxisMaximum(120f);

        // Líneas de referencia para pulso
        leftAxis.addLimitLine(createLimitLine(context, 60f, "Normal Bajo", R.color.bp_optimal));
        leftAxis.addLimitLine(createLimitLine(context, 100f, "Normal Alto", R.color.bp_normal));

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextColor(context.getResources().getColor(R.color.text_primary, null));

        chart.invalidate();
    }
}
