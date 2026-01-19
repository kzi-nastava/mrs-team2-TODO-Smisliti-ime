package com.example.getgo.dtos.inconsistencyReport;

public class CreatedInconsistencyReportDTO {
    private Long id;
    private Long rideId;
    private Long passengerId;
    private String text;
    public CreatedInconsistencyReportDTO() {
    }

    public CreatedInconsistencyReportDTO(Long id, Long rideId, Long passengerId, String text) {
        this.id = id;
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
