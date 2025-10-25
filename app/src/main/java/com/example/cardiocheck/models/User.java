package com.example.cardiocheck.models;

/**
 * Modelo de usuario expandido con información médica completa.
 */
public class User {
    private long id;
    private String fullName;
    private String email;
    private String password;

    // Nuevos campos de perfil médico
    private int age;
    private float height; // en cm
    private float weight; // en kg
    private String gender; // "Masculino", "Femenino", "Otro"
    private String medicalConditions; // Condiciones médicas previas
    private String medications; // Medicación actual
    private String doctorName; // Nombre del médico
    private String emergencyContact; // Contacto de emergencia

    public User() {}

    public User(long id, String fullName, String email, String password) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.age = 0;
        this.height = 0.0f;
        this.weight = 0.0f;
        this.gender = "";
        this.medicalConditions = "";
        this.medications = "";
        this.doctorName = "";
        this.emergencyContact = "";
    }

    // Constructor completo
    public User(long id, String fullName, String email, String password, int age,
                float height, float weight, String gender, String medicalConditions,
                String medications, String doctorName, String emergencyContact) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.medicalConditions = medicalConditions;
        this.medications = medications;
        this.doctorName = doctorName;
        this.emergencyContact = emergencyContact;
    }

    // Getters and Setters existentes
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Nuevos getters and setters
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }

    public String getMedications() { return medications; }
    public void setMedications(String medications) { this.medications = medications; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    // Métodos utilitarios
    public float getBMI() {
        if (height > 0 && weight > 0) {
            float heightInMeters = height / 100;
            return weight / (heightInMeters * heightInMeters);
        }
        return 0.0f;
    }

    public String getBMICategory() {
        float bmi = getBMI();
        if (bmi == 0.0f) return "No calculado";
        if (bmi < 18.5) return "Bajo peso";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Sobrepeso";
        return "Obesidad";
    }

    public boolean isProfileComplete() {
        return age > 0 && height > 0 && weight > 0 && !gender.isEmpty();
    }
}
