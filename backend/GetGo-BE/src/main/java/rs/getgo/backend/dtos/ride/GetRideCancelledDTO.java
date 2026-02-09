package rs.getgo.backend.dtos.ride;

import java.time.LocalDateTime;

public class GetRideCancelledDTO {
    private Long rideId;
    private String status;
    private String cancelledBy;
    private String reason;
    private LocalDateTime cancelledAt;

    public GetRideCancelledDTO() {}

    public GetRideCancelledDTO(Long rideId, String status, String cancelledBy, String reason, LocalDateTime cancelledAt) {
        this.rideId = rideId;
        this.status = status;
        this.cancelledBy = cancelledBy;
        this.reason = reason;
        this.cancelledAt = cancelledAt;
    }

    // Getters and setters
    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}

