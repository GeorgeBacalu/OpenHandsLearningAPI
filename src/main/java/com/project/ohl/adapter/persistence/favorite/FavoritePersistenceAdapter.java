package com.project.ohl.adapter.persistence.favorite;

import com.project.ohl.adapter.persistence.category.CategoryPersistenceAdapter;
import com.project.ohl.application.port.out.FavoriteRepositoryPort;
import com.project.ohl.domain.model.Category;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository @RequiredArgsConstructor @Transactional(readOnly = true)
public class FavoritePersistenceAdapter implements FavoriteRepositoryPort {
    private final FavoriteJpaRepository favorites;
    private final CategoryPersistenceAdapter categories;
    public List<Category> findByUserAndName(UUID userId, String name) {
        return favorites.findByUser_IdAndCategory_NameContainingIgnoreCase(userId, name)
                .stream().map(value -> categories.toDomain(value.getCategory())).toList();
    }
    public boolean exists(UUID userId, UUID categoryId) { return favorites.existsById(new FavoriteId(userId, categoryId)); }
    @Transactional public void save(UUID userId, UUID categoryId) {
        var id = new FavoriteId(userId, categoryId);
        if (!favorites.existsById(id)) {
            var entity = new UserFavoriteEntity(); entity.setId(id); favorites.saveAndFlush(entity);
        }
    }
    @Transactional public void delete(UUID userId, UUID categoryId) { favorites.deleteById(new FavoriteId(userId, categoryId)); }
}
