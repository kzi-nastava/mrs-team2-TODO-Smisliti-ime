package rs.getgo.backend.services;

import org.springframework.security.core.Authentication;
import rs.getgo.backend.dtos.chat.GetChatDTO;
import rs.getgo.backend.dtos.chat.GetUserChatDTO;
import rs.getgo.backend.dtos.message.GetMessageDTO;
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
    GetMessageDTO sendUserMessage(Authentication auth, String text);
    List<GetMessageDTO> getUserMessages(Authentication auth);
    GetMessageDTO sendAdminMessage(Long chatId, String text);
    List<GetChatDTO> getAllChatsDTO();
    List<GetMessageDTO> getChatMessagesDTO(Long chatId);
    GetUserChatDTO getMyChat(Authentication auth);

}
