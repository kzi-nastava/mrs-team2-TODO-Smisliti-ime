package com.example.getgo.dtos.ride;

public class GetRideTrackingDTO {
    private Long rideId;
    private Double vehicleLatitude;
    private Double vehicleLongitude;
    private String startAddress;
    private String destinationAddress;
    private Double estimatedTime; // minutes

    public GetRideTrackingDTO() {
    }

    public GetRideTrackingDTO(Long rideId, Double vehicleLatitude, Double vehicleLongitude, String startAddress, String destinationAddress, Double estimatedTime) {
        this.rideId = rideId;
        this.vehicleLatitude = vehicleLatitude;
        this.vehicleLongitude = vehicleLongitude;
        this.startAddress = startAddress;
        this.destinationAddress = destinationAddress;
        this.estimatedTime = estimatedTime;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Double getVehicleLatitude() {
        return vehicleLatitude;
    }

    public void setVehicleLatitude(Double vehicleLatitude) {
        this.vehicleLatitude = vehicleLatitude;
    }

    public Double getVehicleLongitude() {
        return vehicleLongitude;
    }

    public void setVehicleLongitude(Double vehicleLongitude) {
        this.vehicleLongitude = vehicleLongitude;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public Double getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Double estimatedTime) {
        this.estimatedTime = estimatedTime;
    }
}
