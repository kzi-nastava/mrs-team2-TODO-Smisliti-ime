package com.example.getgo.dtos.passenger;

import java.io.Serializable;

/**
 * DTO sent to PUT /api/passenger/profile
 * Contains fields allowed to be updated by the passenger.
 */
public class UpdatePassengerDTO implements Serializable {
    private String name;
    private String surname;
    private String address;
    private String phone;
    private String profilePictureUrl;

    public UpdatePassengerDTO() {}

    public UpdatePassengerDTO(String name, String surname, String address, String phone, String profilePictureUrl) {
        this.name = name;
        this.surname = surname;
        this.address = address;
        this.phone = phone;
        this.profilePictureUrl = profilePictureUrl;
    }

    // getters / setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
}

