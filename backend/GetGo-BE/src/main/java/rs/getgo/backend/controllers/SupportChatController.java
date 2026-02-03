package rs.getgo.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.dtos.chat.GetChatDTO;
import rs.getgo.backend.dtos.chat.GetUserChatDTO;
import rs.getgo.backend.dtos.message.CreateMessageDTO;
import rs.getgo.backend.dtos.message.GetMessageDTO;
import rs.getgo.backend.model.entities.Chat;
import rs.getgo.backend.model.entities.Message;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.model.enums.SenderType;
import rs.getgo.backend.model.enums.UserRole;
import rs.getgo.backend.services.AuthService;
import rs.getgo.backend.services.SupportChatService;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/support")
public class SupportChatController {

    private final SupportChatService service;
    private final AuthService authService;
    private final SimpMessagingTemplate messagingTemplate;

    public SupportChatController(SupportChatService service, AuthService authService, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.authService = authService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/messages")
    public List<GetMessageDTO> getMessages(Authentication auth) {
        User user = authService.getUserFromAuth(auth);

        return service.getMessages(user).stream()
                .map(m -> new GetMessageDTO(
                        m.getText(),
                        m.getSenderType(),
                        m.getTimestamp()
                ))
                .toList();
    }

    @PostMapping("/messages")
    public void sendMessage(@RequestBody CreateMessageDTO dto,
                            Authentication auth) {

        User user = authService.getUserFromAuth(auth);

        Message savedMessage = service.sendMessage(user, dto.getText());

        messagingTemplate.convertAndSend(
                "/socket-publisher/chat/" + savedMessage.getChat().getId(),
                new GetMessageDTO(
                        savedMessage.getText(),
                        savedMessage.getSenderType(),
                        savedMessage.getTimestamp()
                )
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/chats")
    public List<GetChatDTO> getAllChats() {
        return service.getAllChats().stream()
                .map(chat -> new GetChatDTO(
                        chat.getId(),
                        new GetUserChatDTO(
                                chat.getUser().getId(),
                                chat.getUser().getName()
                        )
                ))
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/messages/{chatId}")
    public List<GetMessageDTO> getChatMessages(@PathVariable Long chatId) {
        return service.getMessagesByChatId(chatId).stream()
                .map(m -> new GetMessageDTO(
                        m.getText(),
                        m.getSenderType(),
                        m.getTimestamp()
                ))
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/messages/{chatId}")
    public void sendMessageAdmin(@PathVariable Long chatId,
                                 @RequestBody CreateMessageDTO dto) {

        Message savedMessage = service.sendMessageAdmin(chatId, dto.getText());

        messagingTemplate.convertAndSend(
                "/socket-publisher/chat/" + chatId,
                new GetMessageDTO(
                        savedMessage.getText(),
                        savedMessage.getSenderType(),
                        savedMessage.getTimestamp()
                )
        );
    }


    @GetMapping("/chat/my")
    public List<GetMessageDTO> getMyChat(Authentication auth) {
        User user = authService.getUserFromAuth(auth);

        return service.getMessages(user).stream()
                .map(m -> new GetMessageDTO(
                        m.getText(),
                        m.getSenderType(),
                        m.getTimestamp()
                ))
                .toList();
    }



}
