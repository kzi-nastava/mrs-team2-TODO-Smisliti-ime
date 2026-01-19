package com.example.getgo.dtos.inconsistencyReport;

public class CreateInconsistencyReportDTO {
    private String text;

    public CreateInconsistencyReportDTO() {
    }

    public CreateInconsistencyReportDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
