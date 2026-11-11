package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.Feedback;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepositoryPort {

    List<Feedback> findAll();

    Optional<Feedback> findById(UUID id);

    Feedback save(Feedback feedback);

    Feedback update(Feedback feedback);

    void delete(Feedback feedback);
}
