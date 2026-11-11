package com.project.ohl.adapter.persistence.feedback;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackJpaRepository extends JpaRepository<FeedbackEntity, UUID> {
}
