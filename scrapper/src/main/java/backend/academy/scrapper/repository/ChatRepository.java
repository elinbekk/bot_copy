package backend.academy.scrapper.repository;

import java.util.List;

public interface ChatRepository {
    void save(Long chatId);

    boolean exists(Long chatId);

    void delete(Long chatId);

    List<Long> findAllChatIds();
}
