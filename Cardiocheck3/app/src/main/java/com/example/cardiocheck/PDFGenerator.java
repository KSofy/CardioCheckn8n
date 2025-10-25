package com.example.cardiocheck;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.example.cardiocheck.models.BloodPressureReading;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Generador de PDF con tabla y gráfico simple.
 */
public class PDFGenerator {

    private final Context context;
    private final OpenAIClient aiClient;

    public PDFGenerator(Context context) {
        this.context = context.getApplicationContext();
        this.aiClient = new OpenAIClient(this.context);
    }

    public File generatePDF(List<BloodPressureReading> selectedReadings, String userName) {
        try {
            PdfDocument doc = new PdfDocument();
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Paint title = new Paint(Paint.ANTI_ALIAS_FLAG);
            title.setTextSize(16f);
            title.setColor(Color.BLACK);
            paint.setTextSize(12f);
            paint.setColor(Color.DKGRAY);

            int width = 595; // A4 en puntos (aprox)
            int height = 842;
            int margin = 32;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
            PdfDocument.Page page = doc.startPage(pageInfo);
            Canvas c = page.getCanvas();

            int y = margin;
            paint.setColor(Color.rgb(24, 144, 255));
            paint.setTextSize(18f);
            c.drawText("Cardio Check – Historial de Presión Arterial", margin, y, paint);
            y += 24;

            paint.setColor(Color.DKGRAY);
            paint.setTextSize(12f);
            String dateStr = DateFormat.getDateTimeInstance().format(new Date());
            c.drawText("Usuario: " + userName, margin, y, paint); y += 18;
            c.drawText("Fecha de generación: " + dateStr, margin, y, paint); y += 24;

            // Tabla encabezado
            int col1 = margin; int col2 = col1 + 160; int col3 = col2 + 100; int col4 = col3 + 100;
            paint.setColor(Color.BLACK);
            paint.setTextSize(12f);
            c.drawText("Fecha", col1, y, paint);
            c.drawText("Sistólica", col2, y, paint);
            c.drawText("Diastólica", col3, y, paint);
            c.drawText("Pulso", col4, y, paint);
            y += 12;
            c.drawLine(margin, y, width - margin, y, paint);
            y += 12;

            DateFormat df = DateFormat.getDateTimeInstance();
            paint.setColor(Color.DKGRAY);
            for (BloodPressureReading r : selectedReadings) {
                String ds = df.format(new Date(r.getTimestamp()));
                c.drawText(ds, col1, y, paint);
                c.drawText(String.valueOf(r.getSystolic()), col2, y, paint);
                c.drawText(String.valueOf(r.getDiastolic()), col3, y, paint);
                c.drawText(String.valueOf(r.getPulse()), col4, y, paint);
                y += 16;
                if (y > height - 200) { // salto simple si se llena
                    doc.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(width, height, (doc.getPages().size() + 1)).create();
                    page = doc.startPage(pageInfo);
                    c = page.getCanvas();
                    y = margin;
                }
            }

            y += 16;
            // Gráfico de tendencia (línea)
            int graphLeft = margin;
            int graphRight = width - margin;
            int graphTop = y;
            int graphBottom = y + 120;
            paint.setColor(Color.LTGRAY);
            c.drawRect(graphLeft, graphTop, graphRight, graphBottom, paint);
            paint.setColor(Color.rgb(24,144,255));

            // Escala de sistólica para la línea
            int n = selectedReadings.size();
            if (n >= 2) {
                int minS = 300, maxS = 0;
                for (BloodPressureReading r : selectedReadings) {
                    minS = Math.min(minS, r.getSystolic());
                    maxS = Math.max(maxS, r.getSystolic());
                }
                if (minS == maxS) { maxS = minS + 1; }
                float dx = (graphRight - graphLeft) / (float) (n - 1);
                float prevX = 0, prevY = 0;
                for (int i = 0; i < n; i++) {
                    BloodPressureReading r = selectedReadings.get(i);
                    float x = graphLeft + i * dx;
                    float norm = (r.getSystolic() - minS) / (float) (maxS - minS);
                    float yPoint = graphBottom - norm * (graphBottom - graphTop - 10) - 5;
                    if (i > 0) c.drawLine(prevX, prevY, x, yPoint, paint);
                    prevX = x; prevY = yPoint;
                }
            }
            y = graphBottom + 20;

            // Resumen IA
            paint.setColor(Color.BLACK);
            paint.setTextSize(14f);
            c.drawText("Recomendación general:", margin, y, paint);
            y += 18;
            paint.setColor(Color.DKGRAY);
            String summary = buildSummary(selectedReadings);
            String ai = aiClient.getChatResponse("Resume y aconseja en 2-3 frases, en español y con empatía, basándote en estas mediciones: " + summary);
            // Partir líneas
            y = drawMultiline(c, ai, margin, y, width - margin, paint);

            doc.finishPage(page);

            String dateFile = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir == null) dir = context.getExternalFilesDir(null);
            if (dir == null) return null;
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "CardioCheck_Report_" + dateFile + ".pdf");
            FileOutputStream fos = new FileOutputStream(file);
            doc.writeTo(fos);
            fos.close();
            doc.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String buildSummary(List<BloodPressureReading> list) {
        StringBuilder sb = new StringBuilder();
        for (BloodPressureReading r : list) {
            sb.append("[").append(r.getSystolic()).append("/").append(r.getDiastolic()).append(" mmHg, ")
                    .append(r.getPulse()).append(" bpm]");
        }
        return sb.toString();
    }

    private int drawMultiline(Canvas c, String text, int left, int y, int right, Paint p) {
        int maxWidth = right - left;
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String test = line.length() == 0 ? w : line + " " + w;
            if (p.measureText(test) > maxWidth) {
                c.drawText(line.toString(), left, y, p);
                y += 16;
                line = new StringBuilder(w);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) {
            c.drawText(line.toString(), left, y, p);
            y += 16;
        }
        return y;
    }
}

