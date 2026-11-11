package com.project.ohl.adapter.persistence.notification;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {
    Slice<NotificationEntity> findByReceiver_Id(UUID receiverId, Pageable pageable);
}
