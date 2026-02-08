package com.example.getgo.dtos.ride;

public class CreatedRideResponseDTO {
    private String status;
    private String message;
    private Long rideId;

    public CreatedRideResponseDTO() {}

    public CreatedRideResponseDTO(String status, String message, Long rideId) {
        this.status = status;
        this.message = message;
        this.rideId = rideId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }
}