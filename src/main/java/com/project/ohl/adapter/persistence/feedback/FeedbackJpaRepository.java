package com.project.ohl.adapter.persistence.feedback;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackJpaRepository extends JpaRepository<FeedbackEntity, UUID> {
    Slice<FeedbackEntity> findByUser_Id(UUID userId, Pageable pageable);
}
