package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.ChatEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatEntityRepository extends JpaRepository<ChatEntity, Long> {
}
