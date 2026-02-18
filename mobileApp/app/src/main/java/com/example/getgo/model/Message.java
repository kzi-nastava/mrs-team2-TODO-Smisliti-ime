package com.example.getgo.model;

public class Message {
    private int id;
    private String text;
    private SenderType senderType;
    private String timestamp;

    public Message() {}

    public Message(int id, String text, SenderType senderType, String timestamp) {
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

    public SenderType getSenderType() {
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

    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
