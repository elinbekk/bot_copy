package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.LinkEntity;
import java.sql.Timestamp;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LinkEntityRepository extends JpaRepository<LinkEntity, Long> {
    void deleteByChatIdAndUrl(Long chatId, String url);

    boolean existsByChatIdAndUrl(Long chatId, String url);

    Collection<LinkEntity> findByChatId(Long chatId);

    @Modifying
    @Query("UPDATE LinkEntity l SET l.lastChecked = :when WHERE l.id = :linkId")
    void updateLastChecked(@Param("linkId") Long linkId, @Param("when") Timestamp when);

    @Query("SELECT l FROM LinkEntity l WHERE l.lastChecked < CURRENT_TIMESTAMP ORDER BY l.lastChecked")
    Page<LinkEntity> findDueLinks(Pageable pageable);
}
