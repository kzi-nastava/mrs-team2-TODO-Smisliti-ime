package com.example.getgo.repositories;

import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.SupportChatApiService;
import com.example.getgo.callbacks.SupportChatMessageListener;
import com.example.getgo.dtos.supportChat.ChatSummary;
import com.example.getgo.dtos.supportChat.CreateMessageRequestDTO;
import com.example.getgo.dtos.supportChat.GetChatDTO;
import com.example.getgo.dtos.supportChat.GetChatIdDTO;
import com.example.getgo.dtos.supportChat.GetMessageDTO;
import com.example.getgo.model.ChatMessage;
import com.example.getgo.model.MessageType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SupportChatRepository {
    private static final String TAG = "SupportChatRepo";
    private static SupportChatRepository instance;

    private final List<SupportChatMessageListener> listeners = new ArrayList<>();

    public void addListener(SupportChatMessageListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(SupportChatMessageListener listener) {
        listeners.remove(listener);
    }

    private void notifyNewMessage(ChatMessage message) {
        for (SupportChatMessageListener listener : listeners) {
            listener.onNewMessage(message);
        }
    }

    private SupportChatRepository() {}

    public static synchronized SupportChatRepository getInstance() {
        if (instance == null) {
            instance = new SupportChatRepository();
        }
        return instance;
    }

    public GetChatIdDTO getMyChat() throws Exception {
        SupportChatApiService service = ApiClient.getClient().create(SupportChatApiService.class);
        Response<GetChatIdDTO> response = service.getMyChat().execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch chat: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch chat");
        }
    }

    public List<ChatMessage> getMyMessages(String myUserType) throws Exception {
        SupportChatApiService service = ApiClient.getClient().create(SupportChatApiService.class);
        Response<List<GetMessageDTO>> response = service.getMyMessages().execute();

        if (response.isSuccessful() && response.body() != null) {
            List<GetMessageDTO> dtos = response.body();
            List<ChatMessage> messages = new ArrayList<>();

            for (GetMessageDTO dto : dtos) {
                boolean isMine = dto.getSenderType().equalsIgnoreCase(myUserType);

                String timestamp = dto.getTimestamp();
                if (timestamp.contains(".")) {

                    timestamp = timestamp.substring(0, timestamp.indexOf("."));
                }

                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date date = isoFormat.parse(timestamp);

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String time = sdf.format(date);

                MessageType type = MessageType.TEXT;
                messages.add(new ChatMessage(dto.getText(), isMine, time, type));
            }
            return messages;
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch messages: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch messages");
        }
    }


    public void sendMessage(CreateMessageRequestDTO request) throws Exception {
        SupportChatApiService service = ApiClient.getClient().create(SupportChatApiService.class);
        Response<Void> response = service.sendMessage(request).execute();

        if (!response.isSuccessful()) {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to send message: " + response.code() + " - " + errBody);
            throw new Exception("Failed to send message");
        } else {
            // Create a local ChatMessage to notify listeners immediately
            ChatMessage msg = new ChatMessage(request.getText(), true,
                    new SimpleDateFormat("HH:mm").format(new Date()), MessageType.TEXT);
            notifyNewMessage(msg);
        }
    }

    // SupportChatRepository.java
    public List<ChatSummary> getAllChats() throws Exception {
        SupportChatApiService service = ApiClient.getClient().create(SupportChatApiService.class);
        Response<List<GetChatDTO>> response = service.getAllChats().execute();

        if (response.isSuccessful() && response.body() != null) {
            List<GetChatDTO> dtos = response.body();
            List<ChatSummary> chats = new ArrayList<>();
            for (GetChatDTO dto : dtos) {
                chats.add(new ChatSummary(dto.getId(), dto.getUserName()));
            }
            return chats;
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch all chats: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch all chats");
        }
    }


    public List<ChatMessage> getMessages(int chatId) throws Exception {
        SupportChatApiService service = ApiClient.getClient().create(SupportChatApiService.class);
        Response<List<GetMessageDTO>> response = service.getChatMessagesAdmin(chatId).execute();

        if (response.isSuccessful() && response.body() != null) {
            List<GetMessageDTO> dtos = response.body();
            List<ChatMessage> messages = new ArrayList<>();


            for (GetMessageDTO dto : dtos) {
                boolean isMine = dto.getSenderType().equalsIgnoreCase("ADMIN");
                String timestamp = dto.getTimestamp();
                if (timestamp.contains(".")) {

                    timestamp = timestamp.substring(0, timestamp.indexOf("."));
                }

                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date date = isoFormat.parse(timestamp);

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String time = sdf.format(date);


                MessageType type = MessageType.TEXT;
                messages.add(new ChatMessage(dto.getText(), isMine, time, type));
            }
            return messages;
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch messages: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch messages");
        }
    }

    public List<GetMessageDTO> getMessagesDTO() throws IOException {
        SupportChatApiService service = ApiClient.getClient().create(SupportChatApiService.class);
        Call<List<GetMessageDTO>> call = service.getMyMessages();
        return call.execute().body();
    }



}
