package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.LinkEntity;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkEntityRepository extends JpaRepository<LinkEntity, Long> {
    void deleteByChatIdAndUrl(Long chatId, String url);

    boolean existsByChatIdAndUrl(Long chatId, String url);

    Collection<LinkEntity> findByChatId(Long chatId);
}
