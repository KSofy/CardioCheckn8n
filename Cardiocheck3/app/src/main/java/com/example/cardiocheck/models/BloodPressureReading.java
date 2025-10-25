package com.example.cardiocheck.models;

/**
 * Modelo de lectura de presión arterial.
 */
public class BloodPressureReading {
    private long id;
    private String email; // usuario dueño del registro
    private int systolic;
    private int diastolic;
    private int pulse;
    private long timestamp;
    private String aiRecommendation; // texto de IA

    public BloodPressureReading() {}

    public BloodPressureReading(long id, String email, int systolic, int diastolic, int pulse, long timestamp, String aiRecommendation) {
        this.id = id;
        this.email = email;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.pulse = pulse;
        this.timestamp = timestamp;
        this.aiRecommendation = aiRecommendation;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getSystolic() { return systolic; }
    public void setSystolic(int systolic) { this.systolic = systolic; }

    public int getDiastolic() { return diastolic; }
    public void setDiastolic(int diastolic) { this.diastolic = diastolic; }

    public int getPulse() { return pulse; }
    public void setPulse(int pulse) { this.pulse = pulse; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getAiRecommendation() { return aiRecommendation; }
    public void setAiRecommendation(String aiRecommendation) { this.aiRecommendation = aiRecommendation; }
}

