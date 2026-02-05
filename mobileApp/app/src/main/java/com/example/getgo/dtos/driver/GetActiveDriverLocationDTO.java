package com.example.getgo.dtos.driver;

public class GetActiveDriverLocationDTO {
    private Long driverId;
    private Double latitude;
    private Double longitude;
    private String vehicleType;
    private Boolean isAvailable;

    public GetActiveDriverLocationDTO() {}

    public GetActiveDriverLocationDTO(Long driverId, Double latitude, Double longitude,
                                      String vehicleType, Boolean isAvailable) {
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.vehicleType = vehicleType;
        this.isAvailable = isAvailable;
    }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}