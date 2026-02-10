package com.example.getgo.dtos.panic;

import java.time.LocalDateTime;

public class PanicAlertDTO {
    private Long panicId;
    private Long rideId;
    private Long triggeredByUserId;
    private LocalDateTime triggeredAt;
    private Boolean status;

    public Long getPanicId() {
        return panicId;
    }

    public void setPanicId(Long panicId) {
        this.panicId = panicId;
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

    public void setTriggeredByUserId(Long triggeredByUserId) {
        this.triggeredByUserId = triggeredByUserId;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}

