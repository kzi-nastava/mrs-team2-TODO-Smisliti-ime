package com.example.getgo.dtos.passenger;

import java.io.Serializable;

/**
 * DTO returned by GET /api/passenger/profile
 */
public class GetPassengerDTO implements Serializable {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private String address;
    private String phone;
    private boolean blocked;
    private String profilePictureUrl;

    public GetPassengerDTO() {}

    public GetPassengerDTO(Long id, String email, String name, String surname,
                           String address, String phone, boolean blocked, String profilePictureUrl) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.address = address;
        this.phone = phone;
        this.blocked = blocked;
        this.profilePictureUrl = profilePictureUrl;
    }

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
}

