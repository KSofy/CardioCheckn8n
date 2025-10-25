package com.example.cardiocheck;

import android.content.Context;
import android.text.TextUtils;

import com.example.cardiocheck.models.BloodPressureReading;
import com.example.cardiocheck.utils.SharedPreferencesHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Cliente simple para llamadas a OpenAI (GPT-3.5-turbo) usando HttpURLConnection.
 */
public class OpenAIClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final Context context;

    public OpenAIClient(Context context) {
        this.context = context.getApplicationContext();
    }

    private String postChat(String prompt) throws Exception {
        // Usar primero la clave de BuildConfig (inyectada vía local.properties),
        // y como respaldo, una posible clave guardada previamente en preferencias.
        String apiKey = BuildConfig.OPENAI_API_KEY;
        if (TextUtils.isEmpty(apiKey)) {
            apiKey = SharedPreferencesHelper.getOpenAIKey(context);
        }
        if (TextUtils.isEmpty(apiKey)) {
            return "No hay una clave de OpenAI configurada en la app.";
        }

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(20000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject body = new JSONObject();
        body.put("model", "gpt-3.5-turbo");
        JSONArray messages = new JSONArray();
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.put(userMsg);
        body.put("messages", messages);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
        writer.write(body.toString());
        writer.flush();
        writer.close();
        os.close();

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        conn.disconnect();

        if (code >= 400) {
            return "No se pudo obtener respuesta de OpenAI. Verifica tu conexión o tu clave.";
        }

        // Parsear JSON -> choices[0].message.content
        JSONObject json = new JSONObject(sb.toString());
        JSONArray choices = json.optJSONArray("choices");
        if (choices != null && choices.length() > 0) {
            JSONObject msg = choices.getJSONObject(0).optJSONObject("message");
            if (msg != null) {
                return msg.optString("content", "").trim();
            }
        }
        return "No se obtuvo una respuesta válida.";
    }

    public String getAnalysisRecommendation(List<BloodPressureReading> lastReadings) {
        try {
            if (lastReadings == null || lastReadings.isEmpty()) {
                return "Aún no hay lecturas suficientes para analizar.";
            }
            // Tomar hasta 3 últimas
            int n = Math.min(3, lastReadings.size());
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < n; i++) {
                BloodPressureReading r = lastReadings.get(i);
                b.append("Lectura ").append(i+1).append(": ")
                        .append("Sistólica ").append(r.getSystolic()).append(" mmHg, ")
                        .append("Diastólica ").append(r.getDiastolic()).append(" mmHg, ")
                        .append("Pulso ").append(r.getPulse()).append(" bpm. ");
            }
            String prompt = "Eres un cardiólogo virtual. Recibes los últimos valores de presión arterial de un paciente: " +
                    b.toString() +
                    " Responde con un análisis breve, empático, en español, sin usar términos técnicos, y una recomendación práctica (máximo 2 oraciones). No menciones que eres una IA.";
            return postChat(prompt);
        } catch (Exception e) {
            return "No se pudo obtener el análisis en este momento.";
        }
    }

    public String getChatResponse(String userQuestion) {
        try {
            String prompt = "Responde como un cardiólogo amable y comprensivo. El usuario pregunta: '" + userQuestion + "'. Responde en español, con empatía, en máximo 3 oraciones. Evita jerga médica.";
            return postChat(prompt);
        } catch (Exception e) {
            return "Estoy teniendo dificultades para responder ahora mismo. Inténtalo de nuevo más tarde.";
        }
    }

    public String getAdviceForSelected(List<BloodPressureReading> readings) {
        try {
            if (readings == null || readings.isEmpty()) {
                return "Selecciona al menos una medición.";
            }
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < readings.size(); i++) {
                BloodPressureReading r = readings.get(i);
                b.append("[Sistólica ").append(r.getSystolic()).append(" mmHg, Diastólica ")
                        .append(r.getDiastolic()).append(" mmHg, Pulso ")
                        .append(r.getPulse()).append(" bpm]");
                if (i < readings.size() - 1) b.append(" ");
            }
            String prompt = "Eres un cardiólogo virtual y amable. Con estas mediciones seleccionadas: " + b.toString() +
                    ". Da un consejo breve y empático en español (máximo 2-3 oraciones), sin jerga médica ni mencionar que eres una IA.";
            return postChat(prompt);
        } catch (Exception e) {
            return "No se pudo obtener el consejo en este momento.";
        }
    }
}
