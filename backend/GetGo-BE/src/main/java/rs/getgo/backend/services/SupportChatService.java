package rs.getgo.backend.services;

import rs.getgo.backend.model.entities.Chat;
import rs.getgo.backend.model.entities.Message;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.model.enums.SenderType;

import java.util.List;

public interface SupportChatService {
    Chat getOrCreateChat(User user);
    List<Message> getMessages(User user);
    Message sendMessage(User user, String text);
    List<Chat> getAllChats();
    List<Message> getMessagesByChatId(Long chatId);
    Message sendMessageAdmin(Long chatId, String text);
}
