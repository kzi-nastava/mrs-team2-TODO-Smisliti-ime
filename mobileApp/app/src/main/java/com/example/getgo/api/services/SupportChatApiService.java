package com.example.getgo.api.services;

import com.example.getgo.dtos.supportChat.CreateMessageRequestDTO;
import com.example.getgo.dtos.supportChat.GetChatDTO;
import com.example.getgo.dtos.supportChat.GetChatIdDTO;
import com.example.getgo.dtos.supportChat.GetMessageDTO;
import com.example.getgo.dtos.supportChat.GetUserChatDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SupportChatApiService {
    @GET("/api/support/chat/my")
    Call<GetUserChatDTO> getMyChat();

    @GET("/api/support/messages")
    Call<List<GetMessageDTO>> getMyMessages();

    @GET("/api/support/chat/{chatId}/messages")
    Call<List<GetMessageDTO>> getMessagesForChat(@Path("chatId") int chatId);

    @GET("/api/support/admin/chats")
    Call<List<GetChatDTO>> getAllChats();

    @POST("/api/support/messages")
    Call<Void> sendMessage(@Body CreateMessageRequestDTO request);

    @GET("/api/support/admin/messages/{chatId}")
    Call<List<GetMessageDTO>> getChatMessagesAdmin(@Path("chatId") int chatId);

    @POST("/api/support/admin/messages/{chatId}")
    Call<GetMessageDTO> sendMessageAdmin(@Path("chatId") int chatId, @Body CreateMessageRequestDTO request);
}
