package com.example.getgo.dtos.ride;

import java.time.LocalDateTime;

public class GetRideStatusUpdateDTO {
    private Long rideId;
    private String status;
    private String message;
    private LocalDateTime timestamp;

    public GetRideStatusUpdateDTO() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}