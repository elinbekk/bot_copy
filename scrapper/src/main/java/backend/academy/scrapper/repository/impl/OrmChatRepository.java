package backend.academy.scrapper.repository.impl;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.repository.ChatEntityRepository;
import backend.academy.scrapper.repository.ChatRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
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

    @Override
    public List<Long> findAllChatIds() {
        List<Long> chatIds = new ArrayList<>();
        for(ChatEntity chatEntity : chatRepo.findAll()) {
            chatIds.add(chatEntity.getId());
        }
        return chatIds;
    }
}
