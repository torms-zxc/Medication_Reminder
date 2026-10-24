package com.example.model;

import java.io.Serializable;

public class HistoryLogModel implements Serializable {
    private int id;
    private String userId;
    private int medicationId;
    private String medicationName;
    private String dosage;
    private String scheduledTime;
    private long actionTime;
    private String status; // "TAKEN", "SKIPPED", "MISSED"
    private String notes;

    public HistoryLogModel() {
    }

    public HistoryLogModel(int id, String userId, int medicationId, String medicationName,
                           String dosage, String scheduledTime, long actionTime,
                           String status, String notes) {
        this.id = id;
        this.userId = userId;
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.scheduledTime = scheduledTime;
        this.actionTime = actionTime;
        this.status = status;
        this.notes = notes;
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

    public int getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(int medicationId) {
        this.medicationId = medicationId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public long getActionTime() {
        return actionTime;
    }

    public void setActionTime(long actionTime) {
        this.actionTime = actionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
