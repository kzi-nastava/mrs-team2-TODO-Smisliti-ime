package com.example.getgo.dtos.user;

/**
 * DTO for forgot-password request.
 * Matches backend ForgotPasswordDTO:
 * - email: required, valid email format
 */
public class ForgotPasswordDTO {
    private String email;

    public ForgotPasswordDTO() {}

    public ForgotPasswordDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

