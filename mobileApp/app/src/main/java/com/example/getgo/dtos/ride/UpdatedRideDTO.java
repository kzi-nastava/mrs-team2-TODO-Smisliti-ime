package com.example.getgo.dtos.ride;

import java.time.LocalDateTime;

public class UpdatedRideDTO {
    private Long id;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public UpdatedRideDTO() {
    }

    public UpdatedRideDTO(Long id, String status, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}