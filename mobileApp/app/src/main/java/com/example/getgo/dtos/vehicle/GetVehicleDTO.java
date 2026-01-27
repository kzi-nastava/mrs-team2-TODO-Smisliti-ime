package com.example.getgo.dtos.vehicle;

public class GetVehicleDTO {
    private Long id;
    private String model;
    private String type;
    private Double latitude;
    private Double longitude;
    private Boolean isAvailable;

    public GetVehicleDTO() {
    }

    public GetVehicleDTO(Long id, String model, String type, Double latitude, Double longitude, Boolean isAvailable) {
        this.id = id;
        this.model = model;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isAvailable = isAvailable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}
