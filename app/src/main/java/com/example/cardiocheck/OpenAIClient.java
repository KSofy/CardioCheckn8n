// C:/Users/USUARIO/AndroidStudioProjects/Cardiocheck3/app/src/main/java/com/example/cardiocheck/OpenAIClient.java

package com.example.cardiocheck;

import android.content.Context;
import android.text.TextUtils;

// Esta línea le dice al código dónde encontrar el archivo BuildConfig.java que se creará

import com.example.cardiocheck.models.BloodPressureReading;
import com.example.cardiocheck.models.User;

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
import java.util.Locale;

public class OpenAIClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public OpenAIClient(Context context) {
    }

    private String postChat(String prompt) throws Exception {
        // Esta línea lee la API Key desde el archivo BuildConfig.java generado
        String apiKey = BuildConfig.OPENAI_API_KEY;

        if (TextUtils.isEmpty(apiKey) || apiKey.equals("null")) {
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

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", "Eres Cardio-IA, un asistente de salud virtual especializado en cardiología. Eres amable, empático y te comunicas en español usando un lenguaje claro y sencillo. Nunca mencionas que eres una IA. Tu objetivo es proporcionar consejos prácticos y tranquilizadores basados en el perfil y las mediciones del usuario.");
        messages.put(systemMsg);

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
            try {
                JSONObject errorJson = new JSONObject(sb.toString()).getJSONObject("error");
                return "Error de OpenAI: " + errorJson.getString("message");
            } catch (Exception e) {
                return "No se pudo obtener respuesta de OpenAI. Código de error: " + code;
            }
        }

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

    // (El resto de los métodos de la clase no necesitan cambios)

    public String getAdviceForSelected(List<BloodPressureReading> readings, User user) {
        try {
            if (readings == null || readings.isEmpty()) {
                return "Selecciona al menos una medición para analizar.";
            }
            if (user == null) {
                return "No se pudo cargar el perfil del usuario para el análisis.";
            }

            StringBuilder userProfile = new StringBuilder();
            userProfile.append("Analiza el caso del siguiente paciente:");
            userProfile.append("- Nombre: ").append(user.getFullName()).append(" ");
            if (user.getAge() > 0) userProfile.append("- Edad: ").append(user.getAge()).append(" años ");
            if (!TextUtils.isEmpty(user.getGender())) userProfile.append("- Género: ").append(user.getGender()).append(" ");
            if (user.getWeight() > 0) userProfile.append("- Peso: ").append(String.format(Locale.US, "%.1f", user.getWeight())).append(" kg ");
            if (user.getHeight() > 0) userProfile.append("- Altura: ").append(String.format(Locale.US, "%.1f", user.getHeight())).append(" cm ");
            if (!TextUtils.isEmpty(user.getMedicalConditions())) userProfile.append("- Condiciones Médicas Declaradas: ").append(user.getMedicalConditions()).append(" ");
            if (!TextUtils.isEmpty(user.getMedications())) userProfile.append("- Medicación Actual: ").append(user.getMedications()).append(" ");

            StringBuilder readingsData = new StringBuilder();
            readingsData.append("Estas son las mediciones de presión arterial que ha seleccionado para el análisis:");
            for (BloodPressureReading r : readings) {
                readingsData.append(String.format(Locale.US, "- Sistólica %d, Diastólica %d, Pulso %d. (Registrado el %s) ",
                        r.getSystolic(), r.getDiastolic(), r.getPulse(), r.getTimestamp()));
            }

            String instructions = " Basado en todo este contexto (perfil y mediciones), actúa como su asistente de salud personal. Por favor, proporciónale un consejo claro, accionable y empático. Dirígete a él por su nombre. Tu respuesta debe ser concisa (máximo 3-4 frases).";
            String finalPrompt = userProfile.toString() + readingsData.toString() + instructions;

            return postChat(finalPrompt);

        } catch (Exception e) {
            e.printStackTrace();
            return "No se pudo obtener el consejo en este momento. Error: " + e.getMessage();
        }
    }

    public String getAdviceForSingleReading(BloodPressureReading reading, User user) {
        try {
            if (reading == null || user == null) {
                return "Faltan datos para el análisis (usuario o medición).";
            }

            StringBuilder userProfile = new StringBuilder();
            userProfile.append("Analiza el caso del siguiente paciente: ");
            userProfile.append("- Nombre: ").append(user.getFullName()).append(" ");
            if (user.getAge() > 0) userProfile.append("- Edad: ").append(user.getAge()).append(" años ");
            if (!TextUtils.isEmpty(user.getMedicalConditions())) userProfile.append("- Condiciones Médicas: ").append(user.getMedicalConditions()).append(" ");

            String readingData = String.format(Locale.US, " El paciente acaba de registrar la siguiente medición: Sistólica %d, Diastólica %d, Pulso %d. ", reading.getSystolic(), reading.getDiastolic(), reading.getPulse());
            String instructions = " Basado en su perfil y esta nueva lectura, dale un consejo inmediato, breve (2-3 frases), y accionable. Dirígete a él por su nombre.";
            String finalPrompt = userProfile.toString() + readingData + instructions;

            return postChat(finalPrompt);

        } catch (Exception e) {
            e.printStackTrace();
            return "No se pudo obtener el consejo en este momento. Error: " + e.getMessage();
        }
    }

    public String getAnalysisRecommendation(List<BloodPressureReading> lastReadings) {
        try {
            if (lastReadings == null || lastReadings.isEmpty()) {
                return "Aún no hay lecturas suficientes para analizar.";
            }
            int n = Math.min(3, lastReadings.size());
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < n; i++) {
                BloodPressureReading r = lastReadings.get(i);
                b.append(String.format(Locale.US, "- Sistólica %d, Diastólica %d, Pulso %d. ", r.getSystolic(), r.getDiastolic(), r.getPulse()));
            }

            String prompt = "Analiza estas últimas mediciones de un paciente: " + b.toString() +
                    " Proporciona un análisis muy breve (1 o 2 frases), empático y una recomendación práctica. Usa un lenguaje sencillo.";

            return postChat(prompt);
        } catch (Exception e) {
            return "No se pudo obtener el análisis en este momento.";
        }
    }

    public String getChatResponse(String userQuestion) {
        try {
            String prompt = "El usuario tiene la siguiente duda de salud: '" + userQuestion +
                    "'. Responde como un asistente de salud amable, dando información general y recomendando siempre consultar a un médico para casos específicos. Tu respuesta debe ser breve y en español.";
            return postChat(prompt);
        } catch (Exception e) {
            return "Estoy teniendo dificultades para responder ahora mismo. Inténtalo de nuevo más tarde.";
        }
    }
}