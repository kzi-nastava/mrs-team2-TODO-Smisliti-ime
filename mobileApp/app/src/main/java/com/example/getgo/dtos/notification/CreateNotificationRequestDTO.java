package com.example.getgo.dtos.notification;

public class CreateNotificationRequestDTO {
    private String title;
    private String message;
    private Long rideId;

    public CreateNotificationRequestDTO() {}

    public CreateNotificationRequestDTO(String title, String message, Long rideId) {
        this.title = title;
        this.message = message;
        this.rideId = rideId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
}

