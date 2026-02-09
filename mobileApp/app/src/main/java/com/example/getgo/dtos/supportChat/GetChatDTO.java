package com.example.getgo.dtos.supportChat;

public class GetChatDTO {
    private Long id;
    private GetUserChatDTO user;

    public GetChatDTO(GetUserChatDTO user, Long id) {
        this.user = user;
        this.id = id;
    }

    public GetChatDTO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GetUserChatDTO getUser() { return user; }
    public void setUser(GetUserChatDTO user) { this.user = user; }
}
