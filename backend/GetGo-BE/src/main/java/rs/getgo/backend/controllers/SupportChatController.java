package rs.getgo.backend.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.getgo.backend.dtos.message.CreateMessageDTO;
import rs.getgo.backend.dtos.message.GetMessageDTO;
import rs.getgo.backend.model.entities.Chat;
import rs.getgo.backend.model.entities.Message;
import rs.getgo.backend.model.entities.User;
import rs.getgo.backend.model.enums.SenderType;
import rs.getgo.backend.model.enums.UserRole;
import rs.getgo.backend.services.AuthService;
import rs.getgo.backend.services.SupportChatService;

import java.util.List;

@RestController
@RequestMapping("/api/support")
public class SupportChatController {

    private final SupportChatService service;
    private final AuthService authService;

    public SupportChatController(SupportChatService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
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

        service.sendMessage(user, dto.getText());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/chats")
    public List<Chat> getAllChats() {
        return service.getAllChats();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/messages/{chatId}")
    public List<Message> getChatMessages(@PathVariable Long chatId) {
        return service.getMessagesByChatId(chatId);
    }





}
