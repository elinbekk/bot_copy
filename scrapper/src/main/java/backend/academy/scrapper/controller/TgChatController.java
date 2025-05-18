package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat")
public class TgChatController {
    private final ChatService chatService;

    public TgChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable("id") Long chatId) {
        chatService.registerChat(chatId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long chatId) {
        if (!chatService.exists(chatId)) {
            return ResponseEntity.badRequest().build();
        }
        chatService.unregisterChat(chatId);
        return ResponseEntity.ok().build();
    }
}
