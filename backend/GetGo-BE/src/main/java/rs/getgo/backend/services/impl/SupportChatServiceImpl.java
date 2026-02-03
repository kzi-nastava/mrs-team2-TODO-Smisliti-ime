package rs.getgo.backend.services.impl;

import org.springframework.stereotype.Service;
import rs.getgo.backend.model.entities.Chat;
import rs.getgo.backend.model.entities.Message;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.model.enums.MessageType;
import rs.getgo.backend.model.enums.SenderType;
import rs.getgo.backend.repositories.ChatRepository;
import rs.getgo.backend.repositories.MessageRepository;
import rs.getgo.backend.services.SupportChatService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportChatServiceImpl implements SupportChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public SupportChatServiceImpl(ChatRepository chatRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    public Chat getOrCreateChat(User user) {
        return chatRepository.findByUser(user)
                .orElseGet(() -> {
                    Chat chat = new Chat();
                    chat.setUser(user);
                    return chatRepository.save(chat);
                });
    }

    public List<Message> getMessages(User user) {
        Chat chat = getOrCreateChat(user);
        return messageRepository.findByChatOrderByTimestampAsc(chat);
    }

    public void sendMessage(User user, String text) {
        Chat chat = getOrCreateChat(user);

        SenderType senderType = switch (user.getRole()) {
            case PASSENGER -> SenderType.USER;
            case DRIVER -> SenderType.DRIVER;
            case ADMIN -> SenderType.ADMIN;
        };

        Message msg = new Message();
        msg.setChat(chat);
        msg.setText(text);
        msg.setSenderType(senderType);
        msg.setType(MessageType.TEXT);
        msg.setTimestamp(LocalDateTime.now());

        messageRepository.save(msg);
    }

    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }


    // Return all messages of a chat (for admin)
    public List<Message> getMessagesByChatId(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        return messageRepository.findByChatOrderByTimestampAsc(chat);
    }



}
