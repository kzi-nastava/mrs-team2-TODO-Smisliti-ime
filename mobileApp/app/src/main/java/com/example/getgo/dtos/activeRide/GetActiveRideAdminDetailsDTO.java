package com.example.getgo.dtos.activeRide;

import java.util.List;

public class GetActiveRideAdminDetailsDTO {
    public int id;
    public String driverName;
    public String driverEmail;
    public String status;
    public String vehicleType;
    public String scheduledTime;
    public String actualStartTime;
    public int estimatedDurationMin;
    public double estimatedPrice;
    public String currentAddress;
    public double currentLat;
    public double currentLng;
    public List<Double> latitudes;
    public List<Double> longitudes;

    public String payingPassenger;
    public List<String> linkedPassengers;
    public boolean needsBabySeats;
    public boolean needsPetFriendly;

    public GetActiveRideAdminDetailsDTO(int id, String driverName, String driverEmail, String status, String vehicleType, String scheduledTime, String actualStartTime, int estimatedDurationMin, double estimatedPrice, String currentAddress, double currentLat, double currentLng, List<Double> latitudes, List<Double> longitudes, String payingPassenger, List<String> linkedPassengers, boolean needsBabySeats, boolean needsPetFriendly) {
        this.id = id;
        this.driverName = driverName;
        this.driverEmail = driverEmail;
        this.status = status;
        this.vehicleType = vehicleType;
        this.scheduledTime = scheduledTime;
        this.actualStartTime = actualStartTime;
        this.estimatedDurationMin = estimatedDurationMin;
        this.estimatedPrice = estimatedPrice;
        this.currentAddress = currentAddress;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        this.latitudes = latitudes;
        this.longitudes = longitudes;
        this.payingPassenger = payingPassenger;
        this.linkedPassengers = linkedPassengers;
        this.needsBabySeats = needsBabySeats;
        this.needsPetFriendly = needsPetFriendly;
    }

    public GetActiveRideAdminDetailsDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(String actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public int getEstimatedDurationMin() {
        return estimatedDurationMin;
    }

    public void setEstimatedDurationMin(int estimatedDurationMin) {
        this.estimatedDurationMin = estimatedDurationMin;
    }

    public double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }

    public double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(double currentLng) {
        this.currentLng = currentLng;
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

    public String getPayingPassenger() {
        return payingPassenger;
    }

    public void setPayingPassenger(String payingPassenger) {
        this.payingPassenger = payingPassenger;
    }

    public List<String> getLinkedPassengers() {
        return linkedPassengers;
    }

    public void setLinkedPassengers(List<String> linkedPassengers) {
        this.linkedPassengers = linkedPassengers;
    }

    public boolean isNeedsBabySeats() {
        return needsBabySeats;
    }

    public void setNeedsBabySeats(boolean needsBabySeats) {
        this.needsBabySeats = needsBabySeats;
    }

    public boolean isNeedsPetFriendly() {
        return needsPetFriendly;
    }

    public void setNeedsPetFriendly(boolean needsPetFriendly) {
        this.needsPetFriendly = needsPetFriendly;
    }
}
