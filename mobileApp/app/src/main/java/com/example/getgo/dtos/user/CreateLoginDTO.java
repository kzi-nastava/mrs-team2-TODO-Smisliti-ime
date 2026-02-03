package com.example.getgo.dtos.user;

/**
 * DTO for login request.
 * Matches backend CreateLoginDTO:
 * - email: required, valid email format
 * - password: required
 */
public class CreateLoginDTO {
    private String email;
    private String password;

    public CreateLoginDTO() {}

    public CreateLoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

