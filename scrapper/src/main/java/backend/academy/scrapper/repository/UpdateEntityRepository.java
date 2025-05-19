package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.UpdateEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpdateEntityRepository extends JpaRepository<UpdateEntity, Long> {
    List<UpdateEntity> findBySentFalse();
}
