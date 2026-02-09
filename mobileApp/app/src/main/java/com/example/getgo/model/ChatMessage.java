package com.example.getgo.model;

public class ChatMessage {
    private String text;
    private boolean mine;
    private String time;
    private MessageType type;

    public ChatMessage(String text, boolean mine, String time, MessageType type) {
        this.text = text;
        this.mine = mine;
        this.time = time;
        this.type = type;
    }

    public ChatMessage() {}

    public String getText() {
        return text;
    }

    public boolean isMine() {
        return mine;
    }

    public String getTime() {
        return time;
    }

    public MessageType getType() {
        return type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
