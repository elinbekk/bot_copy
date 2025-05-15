package backend.academy.scrapper.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chats")
public class ChatEntity {
    @Id
    private Long id;

    public ChatEntity(Long chatId) {
        this.id = chatId;
    }

    public ChatEntity() {
    }
}
