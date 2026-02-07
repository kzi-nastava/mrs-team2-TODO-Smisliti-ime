package com.example.getgo.dtos.authentication;

public class UpdatedPasswordDTO {
    private Boolean success;
    private String message;

    public UpdatedPasswordDTO() {
    }

    public UpdatedPasswordDTO(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
