package com.example.getgo.dtos.request;

public class GetDriverAvatarChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String driverEmail;
    private String driverName;
    private String currentProfilePictureUrl;
    private String requestedProfilePictureUrl;
    private String status;
    private String createdAt;

    public GetDriverAvatarChangeRequestDTO() {}

    public GetDriverAvatarChangeRequestDTO(Long requestId, Long driverId, String driverEmail,
                                           String driverName, String currentProfilePictureUrl,
                                           String requestedProfilePictureUrl, String status,
                                           String createdAt) {
        this.requestId = requestId;
        this.driverId = driverId;
        this.driverEmail = driverEmail;
        this.driverName = driverName;
        this.currentProfilePictureUrl = currentProfilePictureUrl;
        this.requestedProfilePictureUrl = requestedProfilePictureUrl;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getCurrentProfilePictureUrl() {
        return currentProfilePictureUrl;
    }

    public void setCurrentProfilePictureUrl(String currentProfilePictureUrl) {
        this.currentProfilePictureUrl = currentProfilePictureUrl;
    }

    public String getRequestedProfilePictureUrl() {
        return requestedProfilePictureUrl;
    }

    public void setRequestedProfilePictureUrl(String requestedProfilePictureUrl) {
        this.requestedProfilePictureUrl = requestedProfilePictureUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}