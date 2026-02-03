package com.example.getgo.dtos.user;

/**
 * DTO for reset-password request.
 * Matches backend ResetPasswordDTO:
 * - token: required
 * - password: required, min 8 characters
 */
public class ResetPasswordDTO {
    private String token;
    private String password;

    public ResetPasswordDTO() {}

    public ResetPasswordDTO(String token, String password) {
        this.token = token;
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

