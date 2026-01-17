package com.example.getgo.dtos.rating;

public class CreatedRatingDTO {
    private Long id;
    private Long rideId;
    private Long driverId;
    private Long vehicleId;
    private Integer driverRating;
    private Integer vehicleRating;
    private String comment;

    public CreatedRatingDTO() {
    }

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
