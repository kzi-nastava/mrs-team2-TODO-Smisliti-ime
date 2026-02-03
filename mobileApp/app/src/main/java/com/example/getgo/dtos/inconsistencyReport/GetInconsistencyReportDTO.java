package com.example.getgo.dtos.inconsistencyReport;

public class GetInconsistencyReportDTO {

    private Long id;
    private String createdAt;
    private String passengerEmail;
    private String text;

    public GetInconsistencyReportDTO() {
    }

    public Long getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public String getText() {
        return text;
    }
}
