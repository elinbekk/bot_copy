package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatEntityRepository extends JpaRepository<ChatEntity, Long> {
    List<ChatEntity> findAll();
}
