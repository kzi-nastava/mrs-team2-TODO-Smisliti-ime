package com.example.getgo.dtos.supportChat;

public class GetChatIdDTO {
    private int chatId;

    public GetChatIdDTO(int chatId) {
        this.chatId = chatId;
    }

    public GetChatIdDTO() {}

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }
}
