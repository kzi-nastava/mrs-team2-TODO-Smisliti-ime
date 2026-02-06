package com.example.getgo.dtos.request;

public class CreatedDriverChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String status;
    private String message;

    public CreatedDriverChangeRequestDTO() {}

    public CreatedDriverChangeRequestDTO(Long requestId, Long driverId, String status, String message) {
        this.requestId = requestId;
        this.driverId = driverId;
        this.status = status;
        this.message = message;
    }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}