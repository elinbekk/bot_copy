package backend.academy.scrapper.service;

public interface ChatService {
    void registerChat(Long chatId);
    void unregisterChat(Long chatId);
    boolean exists(Long chatId);
}
