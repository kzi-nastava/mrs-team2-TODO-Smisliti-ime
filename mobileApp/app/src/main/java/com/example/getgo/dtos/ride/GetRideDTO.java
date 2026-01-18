package com.example.getgo.dtos.ride;

import com.example.getgo.dtos.passenger.GetRidePassengerDTO;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class GetRideDTO implements Serializable {
    private Long id;
    private Long driverId;
    private List<GetRidePassengerDTO> passengers;
    private String startPoint;
    private String endPoint;
    private LocalDateTime startingTime;
    private LocalDateTime finishedTime;
    private Integer duration;
    private Boolean isCancelled;
    private Boolean isFavourite;
    private String status; // it can be ACTIVE, FINISHED, CANCELLED, SCHEDULED
    private Double price;

    private Boolean panicActivated;

    public GetRideDTO() {
    }

    public GetRideDTO(Long id, Long driverId, List<GetRidePassengerDTO> passengers, String startPoint, String endPoint, LocalDateTime startingTime, LocalDateTime finishedTime, Integer duration, Boolean isCancelled, Boolean isFavourite, String status, Double price, Boolean panicActivated) {
        this.id = id;
        this.driverId = driverId;
        this.passengers = passengers;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.startingTime = startingTime;
        this.finishedTime = finishedTime;
        this.duration = duration;
        this.isCancelled = isCancelled;
        this.isFavourite = isFavourite;
        this.status = status;
        this.price = price;
        this.panicActivated = panicActivated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public List<GetRidePassengerDTO> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<GetRidePassengerDTO> passengers) {
        this.passengers = passengers;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(LocalDateTime startingTime) {
        this.startingTime = startingTime;
    }

    public LocalDateTime getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(LocalDateTime finishedTime) {
        this.finishedTime = finishedTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Boolean getCancelled() {
        return isCancelled;
    }

    public void setCancelled(Boolean cancelled) {
        isCancelled = cancelled;
    }

    public Boolean getFavourite() {
        return isFavourite;
    }

    public void setFavourite(Boolean favourite) {
        isFavourite = favourite;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Boolean getPanicActivated() {
        return panicActivated;
    }

    public void setPanicActivated(Boolean panicActivated) {
        this.panicActivated = panicActivated;
    }






}
