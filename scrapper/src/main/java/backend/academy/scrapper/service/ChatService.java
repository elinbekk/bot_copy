package backend.academy.scrapper.service;

import java.util.List;

public interface ChatService {
    void registerChat(Long chatId);

    void unregisterChat(Long chatId);

    boolean exists(Long chatId);

    List<Long> getChatIds();
}
