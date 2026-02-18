package com.example.getgo.dtos.activeRide;

public class GetActiveRideAdminDTO {
    public int id;
    public String driverName;
    public String status;
    public String scheduledTime;
    public String actualStartTime;
    public String vehicleType;
    public double estimatedPrice;

    public GetActiveRideAdminDTO(int id, String driverName, String status, String scheduledTime, String actualStartTime, String vehicleType, double estimatedPrice) {
        this.id = id;
        this.driverName = driverName;
        this.status = status;
        this.scheduledTime = scheduledTime;
        this.actualStartTime = actualStartTime;
        this.vehicleType = vehicleType;
        this.estimatedPrice = estimatedPrice;
    }

    public GetActiveRideAdminDTO() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }
}
