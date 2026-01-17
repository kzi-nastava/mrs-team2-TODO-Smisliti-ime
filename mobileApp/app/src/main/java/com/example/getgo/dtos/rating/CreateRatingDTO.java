package com.example.getgo.dtos.rating;

public class CreateRatingDTO {
    private Integer driverRating;
    private Integer vehicleRating;
    private String comment;

    public CreateRatingDTO(Integer driverRating, Integer vehicleRating, String comment) {
        this.driverRating = driverRating;
        this.vehicleRating = vehicleRating;
        this.comment = comment;
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
