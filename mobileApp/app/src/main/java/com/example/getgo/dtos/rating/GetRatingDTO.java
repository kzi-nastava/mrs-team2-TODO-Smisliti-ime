package com.example.getgo.dtos.rating;

public class GetRatingDTO {
    private Long id;
    private Long rideId;
    private Long driverId;

    private Long vehicleId;

    private Long passengerId;
    private Integer driverRating;
    private Integer vehicleRating;
    private String comment;

    public Long getId() {
        return id;
    }

    public Long getRideId() {
        return rideId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public Integer getDriverRating() {
        return driverRating;
    }

    public Integer getVehicleRating() {
        return vehicleRating;
    }

    public String getComment() {
        return comment;
    }
}
