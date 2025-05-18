package backend.academy.scrapper.repository.impl;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.repository.ChatEntityRepository;
import backend.academy.scrapper.repository.ChatRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "access-type", havingValue = "ORM")
public class OrmChatRepository implements ChatRepository {
    private final ChatEntityRepository chatRepo;

    public OrmChatRepository(ChatEntityRepository chatEntityRepository) {
        this.chatRepo = chatEntityRepository;
    }

    @Override
    public void save(Long chatId) {
        chatRepo.save(new ChatEntity(chatId));
    }

    @Override
    public boolean exists(Long chatId) {
        return chatRepo.existsById(chatId);
    }

    @Override
    public void delete(Long chatId) {
        chatRepo.deleteById(chatId);
    }
}
