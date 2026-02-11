package com.example.getgo.model;

public class UserProfile {
    private Long id; // added
    private String fullName;
    private String profilePictureUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
