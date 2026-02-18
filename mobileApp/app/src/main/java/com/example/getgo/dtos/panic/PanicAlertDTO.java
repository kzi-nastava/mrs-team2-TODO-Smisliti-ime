package com.example.getgo.dtos.panic;

import java.time.LocalDateTime;

public class PanicAlertDTO {
    private Long panicId;
    private Long rideId;
    private Long driverId;
    private Long triggeredByUserId;
    private LocalDateTime triggeredAt;
    private Boolean status;

    public PanicAlertDTO(Long panicId, Long rideId, Long driverId, Long triggerByUserId, LocalDateTime triggerAt, Boolean status) {
        this.panicId = panicId;
        this.rideId = rideId;
        this.driverId = driverId;
        this.triggeredAt = triggerAt;
        this.triggeredByUserId = triggerByUserId;
        this.status = status;
    }

    public Long getPanicId() {
        return panicId;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Long getTriggeredByUserId() {
        return triggeredByUserId;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Long getDriverId() {
        return this.driverId;
    }
}

