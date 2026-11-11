package com.project.ohl.adapter.persistence.feedback;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackJpaRepository extends JpaRepository<FeedbackEntity, Long> {
}
