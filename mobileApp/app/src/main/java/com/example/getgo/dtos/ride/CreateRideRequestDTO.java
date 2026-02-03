package com.example.getgo.dtos.ride;

import java.util.List;

public class CreateRideRequestDTO {
    private List<Double> latitudes;
    private List<Double> longitudes;
    private List<String> addresses;
    private String scheduledTime;
    private List<String> friendEmails;
    private Boolean hasBaby;
    private Boolean hasPets;
    private String vehicleType;

    public CreateRideRequestDTO() {}

    public CreateRideRequestDTO(List<Double> latitudes, List<Double> longitudes,
                                List<String> addresses, String scheduledTime,
                                List<String> friendEmails, Boolean hasBaby,
                                Boolean hasPets, String vehicleType) {
        this.latitudes = latitudes;
        this.longitudes = longitudes;
        this.addresses = addresses;
        this.scheduledTime = scheduledTime;
        this.friendEmails = friendEmails;
        this.hasBaby = hasBaby;
        this.hasPets = hasPets;
        this.vehicleType = vehicleType;
    }

    public List<Double> getLatitudes() {
        return latitudes;
    }

    public void setLatitudes(List<Double> latitudes) {
        this.latitudes = latitudes;
    }

    public List<Double> getLongitudes() {
        return longitudes;
    }

    public void setLongitudes(List<Double> longitudes) {
        this.longitudes = longitudes;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public List<String> getFriendEmails() {
        return friendEmails;
    }

    public void setFriendEmails(List<String> friendEmails) {
        this.friendEmails = friendEmails;
    }

    public Boolean getHasBaby() {
        return hasBaby;
    }

    public void setHasBaby(Boolean hasBaby) {
        this.hasBaby = hasBaby;
    }

    public Boolean getHasPets() {
        return hasPets;
    }

    public void setHasPets(Boolean hasPets) {
        this.hasPets = hasPets;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
}