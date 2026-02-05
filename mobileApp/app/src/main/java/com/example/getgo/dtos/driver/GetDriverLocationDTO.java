package com.example.getgo.dtos.driver;

public class GetDriverLocationDTO {
    private Long driverId;
    private Long rideId;
    private Double latitude;
    private Double longitude;
    private String status;

    public GetDriverLocationDTO() {}

    public GetDriverLocationDTO(Long driverId, Long rideId, Double latitude, Double longitude, String status) {
        this.driverId = driverId;
        this.rideId = rideId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}