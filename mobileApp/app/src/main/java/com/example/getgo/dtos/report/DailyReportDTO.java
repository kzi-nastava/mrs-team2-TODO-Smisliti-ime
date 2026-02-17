package com.example.getgo.dtos.report;

public class DailyReportDTO {
    private String date;
    private int numberOfRides;
    private double totalKilometers;
    private double totalMoney;

    public DailyReportDTO() {
    }

    public DailyReportDTO(String date, int numberOfRides, double totalKilometers, double totalMoney) {
        this.date = date;
        this.numberOfRides = numberOfRides;
        this.totalKilometers = totalKilometers;
        this.totalMoney = totalMoney;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNumberOfRides() {
        return numberOfRides;
    }

    public void setNumberOfRides(int numberOfRides) {
        this.numberOfRides = numberOfRides;
    }

    public double getTotalKilometers() {
        return totalKilometers;
    }

    public void setTotalKilometers(double totalKilometers) {
        this.totalKilometers = totalKilometers;
    }

    public double getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(double totalMoney) {
        this.totalMoney = totalMoney;
    }
}