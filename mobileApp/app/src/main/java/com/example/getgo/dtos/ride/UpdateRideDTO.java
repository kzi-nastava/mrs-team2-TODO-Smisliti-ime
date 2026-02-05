package com.example.getgo.dtos.ride;

public class UpdateRideDTO {
    private String status;

    public UpdateRideDTO() {
    }

    public UpdateRideDTO(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
