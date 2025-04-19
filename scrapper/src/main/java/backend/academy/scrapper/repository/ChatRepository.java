package backend.academy.scrapper.repository;

public interface ChatRepository {
    void save(Long chatId);
    boolean exists(Long chatId);
    void delete(Long chatId);
}
