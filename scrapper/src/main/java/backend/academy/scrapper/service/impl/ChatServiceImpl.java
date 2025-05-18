package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.service.ChatService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepo;

    public ChatServiceImpl(ChatRepository chatRepo) {
        this.chatRepo = chatRepo;
    }

    @Override
    public void registerChat(Long chatId) {
        chatRepo.save(chatId);
    }

    @Override
    public void unregisterChat(Long chatId) {
        chatRepo.delete(chatId);
    }

    @Override
    public boolean exists(Long chatId) {
        return chatRepo.exists(chatId);
    }

    @Override
    public List<Long> getChatIds() {
        return chatRepo.findAllChatIds();
    }
}
