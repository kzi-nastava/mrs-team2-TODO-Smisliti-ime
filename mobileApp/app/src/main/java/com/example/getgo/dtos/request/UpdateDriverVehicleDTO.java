package com.example.getgo.dtos.request;

public class UpdateDriverVehicleDTO {
    private String vehicleModel;
    private String vehicleType;
    private String vehicleLicensePlate;
    private Integer vehicleSeats;
    private Boolean vehicleHasBabySeats;
    private Boolean vehicleAllowsPets;

    public UpdateDriverVehicleDTO() {}

    public UpdateDriverVehicleDTO(String vehicleModel, String vehicleType, String vehicleLicensePlate,
                                  Integer vehicleSeats, Boolean vehicleHasBabySeats, Boolean vehicleAllowsPets) {
        this.vehicleModel = vehicleModel;
        this.vehicleType = vehicleType;
        this.vehicleLicensePlate = vehicleLicensePlate;
        this.vehicleSeats = vehicleSeats;
        this.vehicleHasBabySeats = vehicleHasBabySeats;
        this.vehicleAllowsPets = vehicleAllowsPets;
    }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleLicensePlate() { return vehicleLicensePlate; }
    public void setVehicleLicensePlate(String vehicleLicensePlate) { this.vehicleLicensePlate = vehicleLicensePlate; }

    public Integer getVehicleSeats() { return vehicleSeats; }
    public void setVehicleSeats(Integer vehicleSeats) { this.vehicleSeats = vehicleSeats; }

    public Boolean getVehicleHasBabySeats() { return vehicleHasBabySeats; }
    public void setVehicleHasBabySeats(Boolean vehicleHasBabySeats) { this.vehicleHasBabySeats = vehicleHasBabySeats; }

    public Boolean getVehicleAllowsPets() { return vehicleAllowsPets; }
    public void setVehicleAllowsPets(Boolean vehicleAllowsPets) { this.vehicleAllowsPets = vehicleAllowsPets; }
}