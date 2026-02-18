package com.example.getgo.dtos.user;

public class BlockUserResponseDTO {
    private Long id;
    private String email;
    private Boolean blocked;
    private String blockReason;
    private String blockedAt;

    public BlockUserResponseDTO() {}

    public BlockUserResponseDTO(Long id, String email, Boolean blocked, String blockReason, String blockedAt) {
        this.id = id;
        this.email = email;
        this.blocked = blocked;
        this.blockReason = blockReason;
        this.blockedAt = blockedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public String getBlockedAt() {
        return blockedAt;
    }

    public void setBlockedAt(String blockedAt) {
        this.blockedAt = blockedAt;
    }
}
