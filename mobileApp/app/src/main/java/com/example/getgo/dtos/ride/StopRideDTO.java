package com.example.getgo.dtos.ride;

public class StopRideDTO {
    private double latitude;
    private double longitude;
    private String stoppedAt; // ISO_LOCAL_DATE_TIME format

    public StopRideDTO() {}

    public StopRideDTO(double latitude, double longitude, String stoppedAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.stoppedAt = stoppedAt;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStoppedAt() {
        return stoppedAt;
    }

    public void setStoppedAt(String stoppedAt) {
        this.stoppedAt = stoppedAt;
    }
}

