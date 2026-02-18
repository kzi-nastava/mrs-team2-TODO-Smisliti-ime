package com.example.getgo.dtos.ride;

import java.time.LocalDateTime;

public class GetRideCancelledDTO {
    private Long rideId;
    private String status;
    private String cancelledBy;
    private String reason;
    private LocalDateTime timestamp;

    public GetRideCancelledDTO() {}

    public GetRideCancelledDTO(Long rideId, String status, String cancelledBy, String reason, LocalDateTime timestamp) {
        this.rideId = rideId;
        this.status = status;
        this.cancelledBy = cancelledBy;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

