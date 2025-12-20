package com.example.getgo.model;

import java.util.List;
public class Ride {
    private int id;
    private String startDate;
    private String startTime;
    private String endTime; // it can be null
    private String startLocation;
    private String endLocation;
    private double price;
    private int rideId;
    private boolean panicActivated;
    private String canceledBy; // DRIVER | PASSENGER | ADMIN
    private String status;     // CREATED | IN_PROGRESS | FINISHED | CANCELED
    private List<String> passengers;

    public Ride(int id, String startDate, String startTime, String endTime,
                String startLocation, String endLocation, double price,
                int rideId, boolean panicActivated, String canceledBy,
                String status, List<String> passengers) {

        this.id = id;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.price = price;
        this.rideId = rideId;
        this.panicActivated = panicActivated;
        this.canceledBy = canceledBy;
        this.status = status;
        this.passengers = passengers;
    }
    public int getId() { return id; }
    public String getStartDate() { return startDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStartLocation() { return startLocation; }
    public String getEndLocation() { return endLocation; }
    public double getPrice() { return price; }
    public int getRideId() { return rideId; }
    public boolean isPanicActivated() { return panicActivated; }
    public String getCanceledBy() { return canceledBy; }
    public String getStatus() { return status; }
    public List<String> getPassengers() { return passengers; }
}
