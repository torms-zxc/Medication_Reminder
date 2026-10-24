package com.example.model;

public class UserModel {
    private int id;
    private String userId;
    private String fullName;
    private long createdAt;

    public UserModel() {
    }

    public UserModel(int id, String userId, String fullName, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.createdAt = createdAt;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
