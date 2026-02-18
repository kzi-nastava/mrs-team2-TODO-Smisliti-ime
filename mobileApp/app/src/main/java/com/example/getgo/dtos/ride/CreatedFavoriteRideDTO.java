package com.example.getgo.dtos.ride;

public class CreatedFavoriteRideDTO {
    private Long favoriteRideId;
    private boolean success;

    public CreatedFavoriteRideDTO() {}

    public CreatedFavoriteRideDTO(Long favoriteRideId, boolean success) {
        this.favoriteRideId = favoriteRideId;
        this.success = success;
    }

    public Long getFavoriteRideId() {
        return favoriteRideId;
    }

    public void setFavoriteRideId(Long favoriteRideId) {
        this.favoriteRideId = favoriteRideId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}