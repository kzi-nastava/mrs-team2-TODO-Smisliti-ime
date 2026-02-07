package com.example.getgo.dtos.driver;

import java.io.Serializable;

public class GetDriverDTO implements Serializable {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private String phone;
    private String address;
    private String profilePictureUrl;
    private Double recentHoursWorked;
    private String vehicleModel;
    private String vehicleType;
    private String vehicleLicensePlate;
    private Integer vehicleSeats;
    private Boolean vehicleHasBabySeats;
    private Boolean vehicleAllowsPets;

    public GetDriverDTO() {}

    public GetDriverDTO(Long id, String email, String name, String surname, String phone, String address,
                        String profilePictureUrl, Double recentHoursWorked, String vehicleModel,
                        String vehicleType, String vehicleLicensePlate, Integer vehicleSeats,
                        Boolean vehicleHasBabySeats, Boolean vehicleAllowsPets) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.address = address;
        this.profilePictureUrl = profilePictureUrl;
        this.recentHoursWorked = recentHoursWorked;
        this.vehicleModel = vehicleModel;
        this.vehicleType = vehicleType;
        this.vehicleLicensePlate = vehicleLicensePlate;
        this.vehicleSeats = vehicleSeats;
        this.vehicleHasBabySeats = vehicleHasBabySeats;
        this.vehicleAllowsPets = vehicleAllowsPets;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public Double getRecentHoursWorked() { return recentHoursWorked; }
    public void setRecentHoursWorked(Double recentHoursWorked) { this.recentHoursWorked = recentHoursWorked; }

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