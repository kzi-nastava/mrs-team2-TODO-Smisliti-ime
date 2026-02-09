package com.example.getgo.dtos.request;

import java.time.LocalDateTime;

public class AcceptDriverChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String status;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;

    public AcceptDriverChangeRequestDTO() {}

    public AcceptDriverChangeRequestDTO(Long requestId, Long driverId, String status, Long reviewedBy, LocalDateTime reviewedAt) {
        this.requestId = requestId;
        this.driverId = driverId;
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}