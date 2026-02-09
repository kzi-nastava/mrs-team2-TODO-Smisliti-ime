package com.example.getgo.dtos.supportChat;

public class GetMessageDTO {
    private int id;
    private String text;
    private String senderType; // "USER" | "ADMIN"
    private String timestamp;

    public GetMessageDTO() {}

    public GetMessageDTO(int id, String text, String senderType, String timestamp) {
        this.id = id;
        this.text = text;
        this.senderType = senderType;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getSenderType() {
        return senderType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
