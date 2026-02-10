package com.example.getgo.dtos.ride;

import java.util.List;

public class GetFavoriteRideDTO {
    private Long id;
    private List<String> addresses;
    private List<Double> latitudes;
    private List<Double> longitudes;
    private String vehicleType;
    private boolean needsBabySeats;
    private boolean needsPetFriendly;
    private List<String> linkedPassengerEmails;

    public GetFavoriteRideDTO() {}

    public GetFavoriteRideDTO(Long id, List<String> addresses, List<Double> latitudes,
                              List<Double> longitudes, String vehicleType, boolean needsBabySeats,
                              boolean needsPetFriendly, List<String> linkedPassengerEmails) {
        this.id = id;
        this.addresses = addresses;
        this.latitudes = latitudes;
        this.longitudes = longitudes;
        this.vehicleType = vehicleType;
        this.needsBabySeats = needsBabySeats;
        this.needsPetFriendly = needsPetFriendly;
        this.linkedPassengerEmails = linkedPassengerEmails;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
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

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
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

    public List<String> getLinkedPassengerEmails() {
        return linkedPassengerEmails;
    }

    public void setLinkedPassengerEmails(List<String> linkedPassengerEmails) {
        this.linkedPassengerEmails = linkedPassengerEmails;
    }
}