package com.example.getgo.dtos.ride;

import java.time.LocalDateTime;

public class GetRideFinishedDTO {
    private Long rideId;
    private String status;
    private Double price;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMinutes;
    private Long driverId;

    public GetRideFinishedDTO() {}

    public GetRideFinishedDTO(Long rideId, String status, Double price, LocalDateTime startTime, LocalDateTime endTime, Long durationMinutes, Long driverId) {
        this.rideId = rideId;
        this.status = status;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.driverId = driverId;
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

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
}