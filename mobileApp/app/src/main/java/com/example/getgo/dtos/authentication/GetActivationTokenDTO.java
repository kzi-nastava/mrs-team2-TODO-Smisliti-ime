package com.example.getgo.dtos.authentication;

public class GetActivationTokenDTO {
    private Boolean valid;
    private String email;
    private String reason;

    public GetActivationTokenDTO() {}

    public GetActivationTokenDTO(Boolean valid, String email, String reason) {
        this.valid = valid;
        this.email = email;
        this.reason = reason;
    }

    public Boolean getValid() { return valid; }
    public void setValid(Boolean valid) { this.valid = valid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}