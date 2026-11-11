package com.project.ohl.adapter.persistence.favorite;

import com.project.ohl.adapter.persistence.category.CategoryEntity;
import com.project.ohl.adapter.persistence.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter @Setter @NoArgsConstructor @Entity @Table(name = "user_favorites")
public class UserFavoriteEntity {
    @EmbeddedId private FavoriteId id;
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "category_id", insertable = false, updatable = false, nullable = false)
    private CategoryEntity category;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
}
