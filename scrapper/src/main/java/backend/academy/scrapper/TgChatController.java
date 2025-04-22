package backend.academy.scrapper;

import backend.academy.scrapper.repository.ChatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat")
public class TgChatController {
    private final ChatRepository chatRepo;

    public TgChatController(ChatRepository chatRepo) {
        this.chatRepo = chatRepo;
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable("id") Long chatId) {
        chatRepo.save(chatId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long chatId) {
        if (!chatRepo.exists(chatId)) {
            return ResponseEntity.badRequest().build();
        }
        chatRepo.delete(chatId);
        return ResponseEntity.ok().build();
    }
}
