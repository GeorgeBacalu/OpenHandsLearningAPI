package com.project.ohl.adapter.persistence.feedback;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.adapter.persistence.user.UserEntity;
import com.project.ohl.application.port.out.FeedbackRepositoryPort;
import com.project.ohl.domain.model.Feedback;
import com.project.ohl.domain.model.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class FeedbackPersistenceAdapter extends JpaPersistenceAdapter<Feedback, FeedbackEntity> implements FeedbackRepositoryPort {

    public FeedbackPersistenceAdapter(FeedbackJpaRepository repository) {
        super(repository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feedback> findPage(UUID userId, int page, int size) {
        var paging = PageRequest.of(page, size, Sort.by(Sort.Order.desc("sentAt"), Sort.Order.asc("id")));
        var records = userId == null ? repository.findAll(paging).getContent()
                : ((FeedbackJpaRepository) repository).findByUser_Id(userId, paging).getContent();
        return records.stream().map(this::toDomain).toList();
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
