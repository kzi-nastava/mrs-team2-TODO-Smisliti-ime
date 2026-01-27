package com.example.getgo.dtos.passenger;

import java.io.Serializable;

public class GetRidePassengerDTO implements Serializable {
    private Long id;
    private String username;

    public GetRidePassengerDTO(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public GetRidePassengerDTO() {
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
