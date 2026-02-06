package com.example.getgo.dtos.user;

public class UpdatedProfilePictureDTO {
    private String pictureUrl;
    private String message;

    public UpdatedProfilePictureDTO() {}

    public UpdatedProfilePictureDTO(String pictureUrl, String message) {
        this.pictureUrl = pictureUrl;
        this.message = message;
    }

    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}