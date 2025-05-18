package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.service.ChatService;
import org.springframework.stereotype.Service;

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
}
