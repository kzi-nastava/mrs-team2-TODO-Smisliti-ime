package com.example.getgo.dtos.passenger;

public class GetPassengerDTO {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private String phone;
    private String address;
    private String profilePictureUrl;

    public GetPassengerDTO() {}

    public GetPassengerDTO(Long id, String email, String name, String surname, String phone, String address, String profilePictureUrl) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.address = address;
        this.profilePictureUrl = profilePictureUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
}