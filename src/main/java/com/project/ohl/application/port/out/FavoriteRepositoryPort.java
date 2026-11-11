package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.Category;
import java.util.List;
import java.util.UUID;

public interface FavoriteRepositoryPort {
    List<Category> findByUserAndName(UUID userId, String name);
    boolean exists(UUID userId, UUID categoryId);
    void save(UUID userId, UUID categoryId);
    void delete(UUID userId, UUID categoryId);
}
