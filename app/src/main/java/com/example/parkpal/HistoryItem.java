package com.example.parkpal;

// HistoryItem.java - Data model for a user parking history entry
public class HistoryItem {
    private String createdAt;
    private int parkingTime;
    private String spotId;
    private double cost;

    public HistoryItem(String createdAt, int parkingTime, String spotId, double cost) {
        this.createdAt = createdAt;
        this.parkingTime = parkingTime;
        this.spotId = spotId;
        this.cost = cost;
    }

    // Getters
    public String getCreatedAt() { return createdAt; }
    public int getParkingTime() { return parkingTime; }
    public String getSpotId() { return spotId; }
    public double getCost() { return cost; }
}