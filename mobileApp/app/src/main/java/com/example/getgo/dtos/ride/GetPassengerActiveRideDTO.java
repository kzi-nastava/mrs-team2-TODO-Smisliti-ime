package com.example.getgo.dtos.ride;

import java.time.LocalDateTime;
import java.util.List;

public class GetPassengerActiveRideDTO {
    private Long rideId;
    private String startingPoint;
    private String endingPoint;
    private Double estimatedPrice;
    private Double estimatedTimeMin;
    private String driverName;
    private String status;
    private List<Double> latitudes;
    private List<Double> longitudes;
    private List<String> addresses;
    private LocalDateTime scheduledTime;

    public GetPassengerActiveRideDTO() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStartingPoint() { return startingPoint; }
    public void setStartingPoint(String startingPoint) { this.startingPoint = startingPoint; }

    public String getEndingPoint() { return endingPoint; }
    public void setEndingPoint(String endingPoint) { this.endingPoint = endingPoint; }

    public Double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(Double estimatedPrice) { this.estimatedPrice = estimatedPrice; }

    public Double getEstimatedTimeMin() { return estimatedTimeMin; }
    public void setEstimatedTimeMin(Double estimatedTimeMin) { this.estimatedTimeMin = estimatedTimeMin; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Double> getLatitudes() { return latitudes; }
    public void setLatitudes(List<Double> latitudes) { this.latitudes = latitudes; }

    public List<Double> getLongitudes() { return longitudes; }
    public void setLongitudes(List<Double> longitudes) { this.longitudes = longitudes; }

    public List<String> getAddresses() { return addresses; }
    public void setAddresses(List<String> addresses) { this.addresses = addresses; }

    public LocalDateTime getScheduledTime() {
        return this.scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
}