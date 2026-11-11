package com.project.ohl.adapter.persistence.letter_sign_pair;

import org.springframework.data.domain.Sort;
import java.util.UUID;
import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.domain.model.LetterSignPair;
import com.project.ohl.application.port.out.LetterSignPairRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class LetterSignPairPersistenceAdapter extends JpaPersistenceAdapter<LetterSignPair, LetterSignPairEntity> implements LetterSignPairRepositoryPort {

    public LetterSignPairPersistenceAdapter(LetterSignPairJpaRepository repository) {
        super(repository);
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by("displayOrder", "id");
    }

    @Override
    protected LetterSignPairEntity newEntity() {
        return new LetterSignPairEntity();
    }

    @Override
    protected UUID idOf(LetterSignPair model) {
        return model.getId();
    }

    @Override
    protected LetterSignPair toDomain(LetterSignPairEntity entity) {
        return LetterSignPair.builder().id(entity.getId())
              .letter(entity.getLetter())
              .imageId(entity.getImageId())
              .build();
    }

    @Override
    protected void apply(LetterSignPair model, LetterSignPairEntity entity) {
        entity.setLetter(model.getLetter());
        entity.setImageId(model.getImageId());
    }
}
