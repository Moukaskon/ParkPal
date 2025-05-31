package com.example.parkpal;

// HistoryItem.java - Data model for a user parking history entry
public class HistoryItem {
    private String createdAt;
    private int parkingTime;
    private int spotId;
    private double cost;

    public HistoryItem(String createdAt, int parkingTime, int spotId, double cost) {
        this.createdAt = createdAt;
        this.parkingTime = parkingTime;
        this.spotId = spotId;
        this.cost = cost;
    }

    // Getters
    public String getCreatedAt() { return createdAt; }
    public int getParkingTime() { return parkingTime; }
    public int getSpotId() { return spotId; }
    public double getCost() { return cost; }
}

