package com.example.getgo.dtos.ride;

import java.time.LocalDateTime;

public class GetRideStoppedEarlyDTO {
    private Long rideId;
    private String status;
    private Double price;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMinutes;
    private String message;
    private LocalDateTime timestamp;

    public GetRideStoppedEarlyDTO() {}

    public GetRideStoppedEarlyDTO(Long rideId, String status, Double price, LocalDateTime startTime,
                                  LocalDateTime endTime, Long durationMinutes, String message, LocalDateTime timestamp) {
        this.rideId = rideId;
        this.status = status;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}