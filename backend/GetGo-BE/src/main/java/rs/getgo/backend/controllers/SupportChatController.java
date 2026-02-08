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

    public SupportChatController(SupportChatService service) {
        this.service = service;
    }

    @GetMapping("/messages")
    public List<GetMessageDTO> getMessages(Authentication auth) {
        return service.getUserMessages(auth);
    }

    @PostMapping("/messages")
    public ResponseEntity<GetMessageDTO> sendMessage(@RequestBody CreateMessageDTO dto, Authentication auth) {
        GetMessageDTO getMessageDTO = service.sendUserMessage(auth, dto.getText());
        return ResponseEntity.ok(getMessageDTO);
    }


//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/chats")
    public List<GetChatDTO> getAllChats() {
        return service.getAllChatsDTO();
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/messages/{chatId}")
    public List<GetMessageDTO> getChatMessages(@PathVariable Long chatId) {
        return service.getChatMessagesDTO(chatId);
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/messages/{chatId}")
    public ResponseEntity<GetMessageDTO> sendMessageAdmin(
            @PathVariable Long chatId,
            @RequestBody CreateMessageDTO dto) {

        GetMessageDTO getMessageDTO = service.sendAdminMessage(chatId, dto.getText());
        return ResponseEntity.ok(getMessageDTO);
    }

    @GetMapping("/chat/my")
    public GetUserChatDTO getMyChat(Authentication auth) {
        return service.getMyChat(auth);
    }
}
