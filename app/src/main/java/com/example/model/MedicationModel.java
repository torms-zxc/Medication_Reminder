package com.example.model;

import java.io.Serializable;

public class MedicationModel implements Serializable {
    private int id;
    private String userId;
    private String name;
    private String dosage;
    private int timeHour;
    private int timeMinute;
    private String timeFormatted;
    private String instructions;
    private String category;
    private boolean isActive;
    private long createdAt;
    private String todayStatus; // "PENDING", "TAKEN", "SKIPPED"

    public MedicationModel() {
        this.isActive = true;
        this.todayStatus = "PENDING";
    }

    public MedicationModel(int id, String userId, String name, String dosage, int timeHour,
                           int timeMinute, String timeFormatted, String instructions,
                           String category, boolean isActive, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.dosage = dosage;
        this.timeHour = timeHour;
        this.timeMinute = timeMinute;
        this.timeFormatted = timeFormatted;
        this.instructions = instructions;
        this.category = category;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.todayStatus = "PENDING";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public int getTimeHour() {
        return timeHour;
    }

    public void setTimeHour(int timeHour) {
        this.timeHour = timeHour;
    }

    public int getTimeMinute() {
        return timeMinute;
    }

    public void setTimeMinute(int timeMinute) {
        this.timeMinute = timeMinute;
    }

    public String getTimeFormatted() {
        return timeFormatted;
    }

    public void setTimeFormatted(String timeFormatted) {
        this.timeFormatted = timeFormatted;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getTodayStatus() {
        return todayStatus;
    }

    public void setTodayStatus(String todayStatus) {
        this.todayStatus = todayStatus;
    }
}
