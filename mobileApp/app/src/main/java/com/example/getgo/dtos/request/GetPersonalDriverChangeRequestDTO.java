package com.example.getgo.dtos.request;

import java.time.LocalDateTime;

public class GetPersonalDriverChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String driverEmail;
    private String driverName;

    private String currentName;
    private String currentSurname;
    private String currentPhone;
    private String currentAddress;

    private String requestedName;
    private String requestedSurname;
    private String requestedPhone;
    private String requestedAddress;

    private String status;
    private LocalDateTime createdAt;

    public GetPersonalDriverChangeRequestDTO() {}

    public GetPersonalDriverChangeRequestDTO(Long requestId, Long driverId, String driverEmail,
                                             String driverName, String currentName,
                                             String currentSurname, String currentPhone,
                                             String currentAddress, String requestedName,
                                             String requestedSurname, String requestedPhone,
                                             String requestedAddress, String status,
                                             LocalDateTime createdAt) {
        this.requestId = requestId;
        this.driverId = driverId;
        this.driverEmail = driverEmail;
        this.driverName = driverName;
        this.currentName = currentName;
        this.currentSurname = currentSurname;
        this.currentPhone = currentPhone;
        this.currentAddress = currentAddress;
        this.requestedName = requestedName;
        this.requestedSurname = requestedSurname;
        this.requestedPhone = requestedPhone;
        this.requestedAddress = requestedAddress;
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

    public String getCurrentName() {
        return currentName;
    }

    public void setCurrentName(String currentName) {
        this.currentName = currentName;
    }

    public String getCurrentSurname() {
        return currentSurname;
    }

    public void setCurrentSurname(String currentSurname) {
        this.currentSurname = currentSurname;
    }

    public String getCurrentPhone() {
        return currentPhone;
    }

    public void setCurrentPhone(String currentPhone) {
        this.currentPhone = currentPhone;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public String getRequestedName() {
        return requestedName;
    }

    public void setRequestedName(String requestedName) {
        this.requestedName = requestedName;
    }

    public String getRequestedSurname() {
        return requestedSurname;
    }

    public void setRequestedSurname(String requestedSurname) {
        this.requestedSurname = requestedSurname;
    }

    public String getRequestedPhone() {
        return requestedPhone;
    }

    public void setRequestedPhone(String requestedPhone) {
        this.requestedPhone = requestedPhone;
    }

    public String getRequestedAddress() {
        return requestedAddress;
    }

    public void setRequestedAddress(String requestedAddress) {
        this.requestedAddress = requestedAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}