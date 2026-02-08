package com.example.getgo.dtos.request;

public class RejectDriverChangeRequestDTO {
    private String reason;

    public RejectDriverChangeRequestDTO() {}

    public RejectDriverChangeRequestDTO(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
