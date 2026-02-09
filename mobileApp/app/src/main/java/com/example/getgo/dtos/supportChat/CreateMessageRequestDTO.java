package com.example.getgo.dtos.supportChat;

public class CreateMessageRequestDTO {
    private Integer chatId; // null za USER
    private String text;

    public CreateMessageRequestDTO(String text) {
        this.text = text;
    }

    public CreateMessageRequestDTO(int chatId, String text) {
        this.chatId = chatId;
        this.text = text;
    }

    public Integer getChatId() {
        return chatId;
    }

    public String getText() {
        return text;
    }
}
