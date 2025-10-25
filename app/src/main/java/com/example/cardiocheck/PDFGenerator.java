package com.example.cardiocheck;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.example.cardiocheck.models.BloodPressureReading;
import com.example.cardiocheck.models.User;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Generador de PDF médico profesional con gráficas, perfil y análisis completo.
 */
public class PDFGenerator {

    private final Context context;
    private final OpenAIClient aiClient;
    private final DatabaseHelper db;

    // Colores profesionales
    private static final int COLOR_PRIMARY = Color.rgb(52, 152, 219);
    private static final int COLOR_SECONDARY = Color.rgb(149, 165, 166);
    private static final int COLOR_SUCCESS = Color.rgb(46, 204, 113);
    private static final int COLOR_WARNING = Color.rgb(241, 196, 15);
    private static final int COLOR_DANGER = Color.rgb(231, 76, 60);
    private static final int COLOR_TEXT = Color.rgb(44, 62, 80);

    public PDFGenerator(Context context) {
        this.context = context.getApplicationContext();
        this.aiClient = new OpenAIClient(this.context);
        this.db = new DatabaseHelper(this.context);
    }

    public File generateAdvancedPDF(List<BloodPressureReading> selectedReadings, String userEmail) {
        try {
            // Obtener datos del usuario
            User user = db.getUserByEmail(userEmail);

            PdfDocument doc = new PdfDocument();
            int width = 595; // A4 width in points
            int height = 842; // A4 height in points
            int margin = 40;
            int currentPage = 1;

            // Página 1: Portada y resumen
            PdfDocument.Page page1 = createPage(doc, width, height, currentPage++);
            Canvas canvas1 = page1.getCanvas();
            drawCoverPage(canvas1, width, height, margin, user, selectedReadings);
            doc.finishPage(page1);

            // Página 2: Perfil médico y estadísticas
            PdfDocument.Page page2 = createPage(doc, width, height, currentPage++);
            Canvas canvas2 = page2.getCanvas();
            drawProfileAndStats(canvas2, width, height, margin, user, selectedReadings);
            doc.finishPage(page2);

            // Página 3: Gráficas y tendencias
            PdfDocument.Page page3 = createPage(doc, width, height, currentPage++);
            Canvas canvas3 = page3.getCanvas();
            drawChartsPage(canvas3, width, height, margin, selectedReadings);
            doc.finishPage(page3);

            // Página 4: Tabla detallada y recomendaciones
            PdfDocument.Page page4 = createPage(doc, width, height, currentPage++);
            Canvas canvas4 = page4.getCanvas();
            drawDetailedData(canvas4, width, height, margin, selectedReadings, user);
            doc.finishPage(page4);

            // Guardar archivo
            String fileName = "Reporte_CardioCheck_" +
                new SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(new Date()) + ".pdf";

            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            doc.writeTo(fos);
            doc.close();
            fos.close();

            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private PdfDocument.Page createPage(PdfDocument doc, int width, int height, int pageNumber) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, pageNumber).create();
        return doc.startPage(pageInfo);
    }

    private void drawCoverPage(Canvas canvas, int width, int height, int margin, User user, List<BloodPressureReading> readings) {
        Paint titlePaint = createPaint(28, COLOR_PRIMARY, true);
        Paint subtitlePaint = createPaint(18, COLOR_TEXT, false);
        Paint bodyPaint = createPaint(14, COLOR_TEXT, false);
        Paint datePaint = createPaint(12, COLOR_SECONDARY, false);

        int y = margin + 60;

        // Logo/Header
        drawRect(canvas, margin, margin, width - margin, margin + 80, COLOR_PRIMARY);
        Paint whitePaint = createPaint(24, Color.WHITE, true);
        canvas.drawText("💚 CARDIOCHECK", margin + 20, margin + 50, whitePaint);

        y += 120;

        // Título principal
        canvas.drawText("REPORTE MÉDICO", margin, y, titlePaint);
        canvas.drawText("PRESIÓN ARTERIAL", margin, y + 40, titlePaint);

        y += 100;

        // Información del paciente
        canvas.drawText("INFORMACIÓN DEL PACIENTE", margin, y, subtitlePaint);
        y += 40;

        String patientName = (user != null && user.getFullName() != null) ? user.getFullName() : "Usuario";
        canvas.drawText("Nombre: " + patientName, margin, y, bodyPaint);
        y += 25;

        if (user != null && user.getAge() > 0) {
            canvas.drawText("Edad: " + user.getAge() + " años", margin, y, bodyPaint);
            y += 25;
        }

        if (user != null && user.getGender() != null && !user.getGender().isEmpty()) {
            canvas.drawText("Género: " + user.getGender(), margin, y, bodyPaint);
            y += 25;
        }

        y += 40;

        // Período del reporte
        canvas.drawText("PERÍODO DEL REPORTE", margin, y, subtitlePaint);
        y += 40;

        if (!readings.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String startDate = dateFormat.format(new Date(readings.get(readings.size() - 1).getTimestamp()));
            String endDate = dateFormat.format(new Date(readings.get(0).getTimestamp()));

            canvas.drawText("Desde: " + startDate, margin, y, bodyPaint);
            y += 25;
            canvas.drawText("Hasta: " + endDate, margin, y, bodyPaint);
            y += 25;
            canvas.drawText("Total de mediciones: " + readings.size(), margin, y, bodyPaint);
        }

        y += 60;

        // Resumen ejecutivo
        canvas.drawText("RESUMEN EJECUTIVO", margin, y, subtitlePaint);
        y += 40;

        if (!readings.isEmpty()) {
            BloodPressureReading latest = readings.get(0);
            int avgSystolic = (int) readings.stream().mapToInt(BloodPressureReading::getSystolic).average().orElse(0);
            int avgDiastolic = (int) readings.stream().mapToInt(BloodPressureReading::getDiastolic).average().orElse(0);

            canvas.drawText("• Última medición: " + latest.getSystolic() + "/" + latest.getDiastolic() + " mmHg", margin, y, bodyPaint);
            y += 25;
            canvas.drawText("• Promedio general: " + avgSystolic + "/" + avgDiastolic + " mmHg", margin, y, bodyPaint);
            y += 25;
            canvas.drawText("• Estado: " + getBloodPressureStatus(avgSystolic, avgDiastolic), margin, y, bodyPaint);
        }

        // Fecha de generación
        String currentDate = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());
        canvas.drawText("Generado: " + currentDate, margin, height - margin - 20, datePaint);
    }

    private void drawProfileAndStats(Canvas canvas, int width, int height, int margin, User user, List<BloodPressureReading> readings) {
        Paint titlePaint = createPaint(20, COLOR_PRIMARY, true);
        Paint subtitlePaint = createPaint(16, COLOR_TEXT, true);
        Paint bodyPaint = createPaint(12, COLOR_TEXT, false);

        int y = margin + 40;

        // Header
        canvas.drawText("PERFIL MÉDICO Y ESTADÍSTICAS", margin, y, titlePaint);
        y += 60;

        // Información médica
        if (user != null) {
            canvas.drawText("DATOS ANTROPOMÉTRICOS", margin, y, subtitlePaint);
            y += 30;

            if (user.getHeight() > 0 && user.getWeight() > 0) {
                canvas.drawText("Altura: " + user.getHeight() + " cm", margin, y, bodyPaint);
                y += 20;
                canvas.drawText("Peso: " + user.getWeight() + " kg", margin, y, bodyPaint);
                y += 20;

                float bmi = user.getBMI();
                if (bmi > 0) {
                    canvas.drawText("IMC: " + String.format("%.1f", bmi) + " - " + user.getBMICategory(), margin, y, bodyPaint);
                    y += 20;
                }
            }

            y += 30;

            // Información médica adicional
            if (user.getMedicalConditions() != null && !user.getMedicalConditions().isEmpty()) {
                canvas.drawText("CONDICIONES MÉDICAS", margin, y, subtitlePaint);
                y += 25;
                drawWrappedText(canvas, user.getMedicalConditions(), margin, y, width - 2 * margin, bodyPaint);
                y += 60;
            }

            if (user.getMedications() != null && !user.getMedications().isEmpty()) {
                canvas.drawText("MEDICACIÓN ACTUAL", margin, y, subtitlePaint);
                y += 25;
                drawWrappedText(canvas, user.getMedications(), margin, y, width - 2 * margin, bodyPaint);
                y += 60;
            }
        }

        // Estadísticas de presión arterial
        if (!readings.isEmpty()) {
            canvas.drawText("ESTADÍSTICAS DE PRESIÓN ARTERIAL", margin, y, subtitlePaint);
            y += 30;

            // Calcular estadísticas
            int minSystolic = readings.stream().mapToInt(BloodPressureReading::getSystolic).min().orElse(0);
            int maxSystolic = readings.stream().mapToInt(BloodPressureReading::getSystolic).max().orElse(0);
            int avgSystolic = (int) readings.stream().mapToInt(BloodPressureReading::getSystolic).average().orElse(0);

            int minDiastolic = readings.stream().mapToInt(BloodPressureReading::getDiastolic).min().orElse(0);
            int maxDiastolic = readings.stream().mapToInt(BloodPressureReading::getDiastolic).max().orElse(0);
            int avgDiastolic = (int) readings.stream().mapToInt(BloodPressureReading::getDiastolic).average().orElse(0);

            canvas.drawText("Sistólica - Mín: " + minSystolic + " | Máx: " + maxSystolic + " | Promedio: " + avgSystolic, margin, y, bodyPaint);
            y += 20;
            canvas.drawText("Diastólica - Mín: " + minDiastolic + " | Máx: " + maxDiastolic + " | Promedio: " + avgDiastolic, margin, y, bodyPaint);
            y += 40;

            // Distribución por categorías
            canvas.drawText("DISTRIBUCIÓN POR CATEGORÍAS", margin, y, subtitlePaint);
            y += 30;

            int optimal = 0, normal = 0, stage1 = 0, stage2 = 0;
            for (BloodPressureReading reading : readings) {
                if (reading.getSystolic() < 120 && reading.getDiastolic() < 80) optimal++;
                else if (reading.getSystolic() < 130 && reading.getDiastolic() < 85) normal++;
                else if (reading.getSystolic() < 140 && reading.getDiastolic() < 90) stage1++;
                else stage2++;
            }

            canvas.drawText("• Óptima (< 120/80): " + optimal + " mediciones (" + (optimal * 100 / readings.size()) + "%)", margin, y, bodyPaint);
            y += 20;
            canvas.drawText("• Normal (120-129/80-84): " + normal + " mediciones (" + (normal * 100 / readings.size()) + "%)", margin, y, bodyPaint);
            y += 20;
            canvas.drawText("• Hipertensión I (130-139/85-89): " + stage1 + " mediciones (" + (stage1 * 100 / readings.size()) + "%)", margin, y, bodyPaint);
            y += 20;
            canvas.drawText("• Hipertensión II (≥ 140/90): " + stage2 + " mediciones (" + (stage2 * 100 / readings.size()) + "%)", margin, y, bodyPaint);
        }
    }

    private void drawChartsPage(Canvas canvas, int width, int height, int margin, List<BloodPressureReading> readings) {
        Paint titlePaint = createPaint(20, COLOR_PRIMARY, true);
        Paint bodyPaint = createPaint(12, COLOR_TEXT, false);

        int y = margin + 40;

        canvas.drawText("GRÁFICAS Y TENDENCIAS", margin, y, titlePaint);
        y += 60;

        if (!readings.isEmpty()) {
            // Dibujar gráfica simple (placeholder para gráfica real)
            drawSimpleChart(canvas, readings, margin, y, width - 2 * margin, 200);
            y += 250;

            canvas.drawText("ANÁLISIS DE TENDENCIAS", margin, y, titlePaint);
            y += 40;

            // Análisis de tendencia
            if (readings.size() >= 2) {
                BloodPressureReading first = readings.get(readings.size() - 1);
                BloodPressureReading last = readings.get(0);

                int systolicTrend = last.getSystolic() - first.getSystolic();
                int diastolicTrend = last.getDiastolic() - first.getDiastolic();

                String trendText = "Tendencia sistólica: " + (systolicTrend > 0 ? "↑ +" : "↓ ") + systolicTrend + " mmHg";
                canvas.drawText(trendText, margin, y, bodyPaint);
                y += 25;

                trendText = "Tendencia diastólica: " + (diastolicTrend > 0 ? "↑ +" : "↓ ") + diastolicTrend + " mmHg";
                canvas.drawText(trendText, margin, y, bodyPaint);
                y += 40;

                // Interpretación
                canvas.drawText("INTERPRETACIÓN:", margin, y, titlePaint);
                y += 30;

                if (Math.abs(systolicTrend) < 5 && Math.abs(diastolicTrend) < 5) {
                    canvas.drawText("• Presión arterial estable en el período analizado", margin, y, bodyPaint);
                } else if (systolicTrend > 5 || diastolicTrend > 5) {
                    canvas.drawText("• Se observa tendencia al alza - consulte con su médico", margin, y, bodyPaint);
                } else {
                    canvas.drawText("• Se observa tendencia a la baja - mantener hábitos saludables", margin, y, bodyPaint);
                }
            }
        }
    }

    private void drawDetailedData(Canvas canvas, int width, int height, int margin, List<BloodPressureReading> readings, User user) {
        Paint titlePaint = createPaint(20, COLOR_PRIMARY, true);
        Paint headerPaint = createPaint(10, COLOR_TEXT, true);
        Paint dataPaint = createPaint(9, COLOR_TEXT, false);

        int y = margin + 40;

        canvas.drawText("REGISTRO DETALLADO DE MEDICIONES", margin, y, titlePaint);
        y += 50;

        // Headers de tabla
        int col1 = margin;
        int col2 = margin + 100;
        int col3 = margin + 180;
        int col4 = margin + 240;
        int col5 = margin + 300;

        canvas.drawText("FECHA", col1, y, headerPaint);
        canvas.drawText("HORA", col2, y, headerPaint);
        canvas.drawText("SISTÓLICA", col3, y, headerPaint);
        canvas.drawText("DIASTÓLICA", col4, y, headerPaint);
        canvas.drawText("PULSO", col5, y, headerPaint);

        // Línea separadora
        y += 15;
        canvas.drawLine(margin, y, width - margin, y, headerPaint);
        y += 20;

        // Datos de la tabla
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        int maxRows = Math.min(25, readings.size()); // Máximo 25 filas por página

        for (int i = 0; i < maxRows; i++) {
            BloodPressureReading reading = readings.get(i);
            Date date = new Date(reading.getTimestamp());

            canvas.drawText(dateFormat.format(date), col1, y, dataPaint);
            canvas.drawText(timeFormat.format(date), col2, y, dataPaint);
            canvas.drawText(String.valueOf(reading.getSystolic()), col3, y, dataPaint);
            canvas.drawText(String.valueOf(reading.getDiastolic()), col4, y, dataPaint);
            canvas.drawText(String.valueOf(reading.getPulse()), col5, y, dataPaint);

            y += 18;

            if (y > height - 100) break; // Evitar que se salga de la página
        }

        // Recomendaciones finales
        if (y < height - 150) {
            y += 40;
            canvas.drawText("RECOMENDACIONES MÉDICAS", margin, y, titlePaint);
            y += 30;

            String recommendations = generateRecommendations(readings, user);
            drawWrappedText(canvas, recommendations, margin, y, width - 2 * margin, dataPaint);
        }
    }

    private void drawSimpleChart(Canvas canvas, List<BloodPressureReading> readings, int x, int y, int width, int height) {
        Paint chartPaint = createPaint(2, COLOR_PRIMARY, false);
        Paint axisPaint = createPaint(1, COLOR_SECONDARY, false);
        Paint textPaint = createPaint(8, COLOR_TEXT, false);

        // Dibujar ejes
        canvas.drawLine(x, y + height, x + width, y + height, axisPaint); // Eje X
        canvas.drawLine(x, y, x, y + height, axisPaint); // Eje Y

        if (readings.size() > 1) {
            // Calcular puntos para sistólica
            int minVal = readings.stream().mapToInt(BloodPressureReading::getSystolic).min().orElse(80);
            int maxVal = readings.stream().mapToInt(BloodPressureReading::getSystolic).max().orElse(180);

            float xStep = (float) width / (readings.size() - 1);
            float yRange = maxVal - minVal;

            for (int i = 0; i < readings.size() - 1; i++) {
                BloodPressureReading current = readings.get(readings.size() - 1 - i);
                BloodPressureReading next = readings.get(readings.size() - 2 - i);

                float x1 = x + i * xStep;
                float y1 = y + height - ((current.getSystolic() - minVal) / yRange * height);
                float x2 = x + (i + 1) * xStep;
                float y2 = y + height - ((next.getSystolic() - minVal) / yRange * height);

                canvas.drawLine(x1, y1, x2, y2, chartPaint);
            }
        }

        // Etiquetas
        canvas.drawText("Presión Sistólica (mmHg)", x, y - 10, textPaint);
    }

    private Paint createPaint(float textSize, int color, boolean bold) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(color);
        if (bold) {
            paint.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return paint;
    }

    private void drawRect(Canvas canvas, int left, int top, int right, int bottom, int color) {
        Paint rectPaint = new Paint();
        rectPaint.setColor(color);
        canvas.drawRect(left, top, right, bottom, rectPaint);
    }

    private void drawWrappedText(Canvas canvas, String text, int x, int y, int maxWidth, Paint paint) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int currentY = y;

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            Rect bounds = new Rect();
            paint.getTextBounds(testLine, 0, testLine.length(), bounds);

            if (bounds.width() <= maxWidth) {
                line.append(line.length() == 0 ? word : " " + word);
            } else {
                if (line.length() > 0) {
                    canvas.drawText(line.toString(), x, currentY, paint);
                    currentY += paint.getTextSize() + 5;
                }
                line = new StringBuilder(word);
            }
        }

        if (line.length() > 0) {
            canvas.drawText(line.toString(), x, currentY, paint);
        }
    }

    private String getBloodPressureStatus(int systolic, int diastolic) {
        if (systolic < 120 && diastolic < 80) return "Óptima";
        if (systolic < 130 && diastolic < 85) return "Normal";
        if (systolic < 140 && diastolic < 90) return "Hipertensión Grado I";
        return "Hipertensión Grado II";
    }

    private String generateRecommendations(List<BloodPressureReading> readings, User user) {
        StringBuilder recommendations = new StringBuilder();

        if (!readings.isEmpty()) {
            int avgSystolic = (int) readings.stream().mapToInt(BloodPressureReading::getSystolic).average().orElse(0);
            int avgDiastolic = (int) readings.stream().mapToInt(BloodPressureReading::getDiastolic).average().orElse(0);

            recommendations.append("Basado en sus mediciones:\n\n");

            if (avgSystolic < 120 && avgDiastolic < 80) {
                recommendations.append("• Mantener hábitos saludables actuales\n");
                recommendations.append("• Continuar con ejercicio regular\n");
            } else if (avgSystolic >= 140 || avgDiastolic >= 90) {
                recommendations.append("• Consultar con médico especialista\n");
                recommendations.append("• Considerar evaluación cardiovascular\n");
                recommendations.append("• Monitorear diariamente\n");
            } else {
                recommendations.append("• Mantener control regular\n");
                recommendations.append("• Revisar hábitos alimenticios\n");
            }

            recommendations.append("\nEste reporte no reemplaza la consulta médica profesional.");
        }

        return recommendations.toString();
    }

    // Método público original mejorado
    public File generatePDF(List<BloodPressureReading> selectedReadings, String userName) {
        // Redirigir al nuevo método avanzado
        return generateAdvancedPDF(selectedReadings, userName);
    }
}
