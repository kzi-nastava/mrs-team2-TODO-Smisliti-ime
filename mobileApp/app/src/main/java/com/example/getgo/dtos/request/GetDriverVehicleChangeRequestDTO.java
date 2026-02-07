package com.example.getgo.dtos.request;

public class GetDriverVehicleChangeRequestDTO {
    private Long requestId;
    private Long driverId;
    private String driverEmail;
    private String driverName;

    private String currentVehicleModel;
    private String currentVehicleType;
    private String currentVehicleLicensePlate;
    private Integer currentVehicleSeats;
    private Boolean currentVehicleHasBabySeats;
    private Boolean currentVehicleAllowsPets;

    private String requestedVehicleModel;
    private String requestedVehicleType;
    private String requestedVehicleLicensePlate;
    private Integer requestedVehicleSeats;
    private Boolean requestedVehicleHasBabySeats;
    private Boolean requestedVehicleAllowsPets;

    private String status;
    private String createdAt;

    public GetDriverVehicleChangeRequestDTO() {}

    public GetDriverVehicleChangeRequestDTO(Long requestId, Long driverId, String driverEmail,
                                            String driverName, String currentVehicleModel,
                                            String currentVehicleType, String currentVehicleLicensePlate,
                                            Integer currentVehicleSeats, Boolean currentVehicleHasBabySeats,
                                            Boolean currentVehicleAllowsPets, String requestedVehicleModel,
                                            String requestedVehicleType, String requestedVehicleLicensePlate,
                                            Integer requestedVehicleSeats, Boolean requestedVehicleHasBabySeats,
                                            Boolean requestedVehicleAllowsPets, String status,
                                            String createdAt) {
        this.requestId = requestId;
        this.driverId = driverId;
        this.driverEmail = driverEmail;
        this.driverName = driverName;
        this.currentVehicleModel = currentVehicleModel;
        this.currentVehicleType = currentVehicleType;
        this.currentVehicleLicensePlate = currentVehicleLicensePlate;
        this.currentVehicleSeats = currentVehicleSeats;
        this.currentVehicleHasBabySeats = currentVehicleHasBabySeats;
        this.currentVehicleAllowsPets = currentVehicleAllowsPets;
        this.requestedVehicleModel = requestedVehicleModel;
        this.requestedVehicleType = requestedVehicleType;
        this.requestedVehicleLicensePlate = requestedVehicleLicensePlate;
        this.requestedVehicleSeats = requestedVehicleSeats;
        this.requestedVehicleHasBabySeats = requestedVehicleHasBabySeats;
        this.requestedVehicleAllowsPets = requestedVehicleAllowsPets;
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

    public String getCurrentVehicleModel() {
        return currentVehicleModel;
    }

    public void setCurrentVehicleModel(String currentVehicleModel) {
        this.currentVehicleModel = currentVehicleModel;
    }

    public String getCurrentVehicleType() {
        return currentVehicleType;
    }

    public void setCurrentVehicleType(String currentVehicleType) {
        this.currentVehicleType = currentVehicleType;
    }

    public String getCurrentVehicleLicensePlate() {
        return currentVehicleLicensePlate;
    }

    public void setCurrentVehicleLicensePlate(String currentVehicleLicensePlate) {
        this.currentVehicleLicensePlate = currentVehicleLicensePlate;
    }

    public Integer getCurrentVehicleSeats() {
        return currentVehicleSeats;
    }

    public void setCurrentVehicleSeats(Integer currentVehicleSeats) {
        this.currentVehicleSeats = currentVehicleSeats;
    }

    public Boolean getCurrentVehicleHasBabySeats() {
        return currentVehicleHasBabySeats;
    }

    public void setCurrentVehicleHasBabySeats(Boolean currentVehicleHasBabySeats) {
        this.currentVehicleHasBabySeats = currentVehicleHasBabySeats;
    }

    public Boolean getCurrentVehicleAllowsPets() {
        return currentVehicleAllowsPets;
    }

    public void setCurrentVehicleAllowsPets(Boolean currentVehicleAllowsPets) {
        this.currentVehicleAllowsPets = currentVehicleAllowsPets;
    }

    public String getRequestedVehicleModel() {
        return requestedVehicleModel;
    }

    public void setRequestedVehicleModel(String requestedVehicleModel) {
        this.requestedVehicleModel = requestedVehicleModel;
    }

    public String getRequestedVehicleType() {
        return requestedVehicleType;
    }

    public void setRequestedVehicleType(String requestedVehicleType) {
        this.requestedVehicleType = requestedVehicleType;
    }

    public String getRequestedVehicleLicensePlate() {
        return requestedVehicleLicensePlate;
    }

    public void setRequestedVehicleLicensePlate(String requestedVehicleLicensePlate) {
        this.requestedVehicleLicensePlate = requestedVehicleLicensePlate;
    }

    public Integer getRequestedVehicleSeats() {
        return requestedVehicleSeats;
    }

    public void setRequestedVehicleSeats(Integer requestedVehicleSeats) {
        this.requestedVehicleSeats = requestedVehicleSeats;
    }

    public Boolean getRequestedVehicleHasBabySeats() {
        return requestedVehicleHasBabySeats;
    }

    public void setRequestedVehicleHasBabySeats(Boolean requestedVehicleHasBabySeats) {
        this.requestedVehicleHasBabySeats = requestedVehicleHasBabySeats;
    }

    public Boolean getRequestedVehicleAllowsPets() {
        return requestedVehicleAllowsPets;
    }

    public void setRequestedVehicleAllowsPets(Boolean requestedVehicleAllowsPets) {
        this.requestedVehicleAllowsPets = requestedVehicleAllowsPets;
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
