package com.example.getgo.dtos.supportChat;

public class ChatSummary {
    private int id;
    private String userName;


    public ChatSummary(int id, String userName) {
        this.id = id;
        this.userName = userName;
    }

    public ChatSummary() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
