package com.example.getgo.callbacks;

import com.example.getgo.model.ChatMessage;

public interface SupportChatMessageListener {
    void onNewMessage(ChatMessage message);
}
