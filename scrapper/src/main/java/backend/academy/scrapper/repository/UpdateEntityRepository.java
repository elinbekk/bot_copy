package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.UpdateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpdateEntityRepository extends JpaRepository<UpdateEntity, Long> {
    Page<UpdateEntity> findBySentFalse(Pageable pg);
}
