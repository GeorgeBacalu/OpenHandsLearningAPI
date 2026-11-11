package com.project.ohl.adapter.persistence.feedback;

import java.util.UUID;
import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.adapter.persistence.user.UserEntity;
import com.project.ohl.domain.model.Feedback;
import com.project.ohl.application.port.out.FeedbackRepositoryPort;
import com.project.ohl.domain.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class FeedbackPersistenceAdapter extends JpaPersistenceAdapter<Feedback, FeedbackEntity> implements FeedbackRepositoryPort {

    public FeedbackPersistenceAdapter(FeedbackJpaRepository repository) {
        super(repository);
    }

    @Override
    protected FeedbackEntity newEntity() {
        return new FeedbackEntity();
    }

    @Override
    protected UUID idOf(Feedback model) {
        return model.getId();
    }

    @Override
    protected Feedback toDomain(FeedbackEntity entity) {
        return Feedback.builder().id(entity.getId())
              .type(entity.getType())
              .description(entity.getDescription())
              .sentAt(entity.getSentAt())
              .user(entity.getUser() == null ? null : User.builder().id(entity.getUser().getId()).build())
              .build();
    }

    @Override
    protected void apply(Feedback model, FeedbackEntity entity) {
        entity.setType(model.getType());
        entity.setDescription(model.getDescription());
        entity.setSentAt(model.getSentAt());
        entity.setUser(reference(UserEntity.class, model.getUser() == null ? null : model.getUser().getId()));
    }
}
