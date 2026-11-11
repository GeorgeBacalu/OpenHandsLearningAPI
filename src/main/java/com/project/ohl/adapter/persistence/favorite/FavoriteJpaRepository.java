package com.project.ohl.adapter.persistence.favorite;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteJpaRepository extends JpaRepository<UserFavoriteEntity, FavoriteId> {
    @EntityGraph(attributePaths = "category")
    List<UserFavoriteEntity> findByUser_IdAndCategory_NameContainingIgnoreCaseOrderByCategory_DisplayOrderAscCategory_IdAsc(UUID userId, String name);
}
