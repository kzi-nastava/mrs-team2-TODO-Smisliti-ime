package com.example.getgo.dtos.ride;

import com.example.getgo.dtos.passenger.GetRidePassengerDTO;
import com.example.getgo.dtos.route.RouteDTO;
import com.example.getgo.model.VehicleType;

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
    private VehicleType vehicleType;
    private Boolean needsBabySeats;
    private Boolean needsPetFriendly;
    private Boolean panicActivated;
    private RouteDTO route;
    private Double estDistance;
    private Double estTime;
    private String cancelledBy; // "DRIVER", "PASSENGER", or null
    private String cancelledReason;

    public GetRideDTO() {
    }

    public GetRideDTO(Long id, Long driverId, List<GetRidePassengerDTO> passengers, String startPoint, String endPoint, LocalDateTime startingTime, LocalDateTime finishedTime, Integer duration, Boolean isCancelled, Boolean isFavourite, String status, Double price, Boolean panicActivated, RouteDTO route, Double estDistance, Double estTime, String cancelledBy, String cancelledReason) {
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
        this.route = route;
        this.estDistance = estDistance;
        this.estTime = estTime;
        this.cancelledBy = cancelledBy;
        this.cancelledReason = cancelledReason;

    }

    public GetRideDTO(Long id, Long driverId, List<GetRidePassengerDTO> passengers, String startPoint, String endPoint, LocalDateTime startingTime, LocalDateTime finishedTime, Integer duration, Boolean isCancelled, Boolean isFavourite, String status, Double price, Boolean panicActivated, VehicleType vehicleType, Boolean needsBabySeats, Boolean needsPetFriendly, RouteDTO route, Double estDistance, Double estTime, String cancelledBy, String cancelledReason) {
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
        this.route = route;
        this.vehicleType = vehicleType;
        this.needsBabySeats = needsBabySeats;
        this.needsPetFriendly = needsPetFriendly;
        this.estDistance = estDistance;
        this.estTime = estTime;
        this.cancelledBy = cancelledBy;
        this.cancelledReason = cancelledReason;
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

    public RouteDTO getRoute() {
        return route;
    }

    public void setRoute(RouteDTO route) {
        this.route = route;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Boolean getNeedsBabySeats() {
        return needsBabySeats;
    }

    public void setNeedsBabySeats(Boolean needsBabySeats) {
        this.needsBabySeats = needsBabySeats;
    }

    public Boolean getNeedsPetFriendly() {
        return needsPetFriendly;
    }

    public void setNeedsPetFriendly(Boolean needsPetFriendly) {
        this.needsPetFriendly = needsPetFriendly;
    }

    public Double getEstDistance() {
        return estDistance;
    }

    public void setEstDistance(Double estDistance) {
        this.estDistance = estDistance;
    }

    public Double getEstTime() {
        return estTime;
    }

    public void setEstTime(Double estTime) {
        this.estTime = estTime;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getCancelledReason() {
        return cancelledReason;
    }

    public void setCancelledReason(String cancelledReason) {
        this.cancelledReason = cancelledReason;
    }
}
