package rs.getgo.backend.dtos.ride;

import java.time.LocalDateTime;

public class CancelRideDTO {
    private Long cancelerId;
    private String role; // "DRIVER" or "PASSENGER"
    private String reason;
    private LocalDateTime scheduledStartTime; // optional; needed to validate passenger cancellations
    private Boolean passengersEntered; // optional; needed to validate driver cancellations

    public Long getCancelerId() { return cancelerId; }
    public void setCancelerId(Long cancelerId) { this.cancelerId = cancelerId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(LocalDateTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }

    public Boolean getPassengersEntered() { return passengersEntered; }
    public void setPassengersEntered(Boolean passengersEntered) { this.passengersEntered = passengersEntered; }
}

