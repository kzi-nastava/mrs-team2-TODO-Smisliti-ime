package com.example.getgo.dtos.supportChat;

public class GetUserChatDTO {
    private Long id;
    private String name;

    public GetUserChatDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public GetUserChatDTO() {
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
