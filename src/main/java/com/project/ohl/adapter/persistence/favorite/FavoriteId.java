package com.project.ohl.adapter.persistence.favorite;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record FavoriteId(@Column(name = "user_id") UUID userId,
                         @Column(name = "category_id") UUID categoryId) implements Serializable {}
