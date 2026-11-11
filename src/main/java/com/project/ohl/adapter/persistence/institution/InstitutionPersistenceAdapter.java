package com.project.ohl.adapter.persistence.institution;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.application.port.out.InstitutionRepositoryPort;
import com.project.ohl.domain.model.Institution;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class InstitutionPersistenceAdapter extends JpaPersistenceAdapter<Institution, InstitutionEntity> implements InstitutionRepositoryPort {

    public InstitutionPersistenceAdapter(InstitutionJpaRepository repository) {
        super(repository);
    }

    @Override
    protected InstitutionEntity newEntity() {
        return new InstitutionEntity();
    }

    @Override
    protected UUID idOf(Institution model) {
        return model.getId();
    }

    @Override
    protected Institution toDomain(InstitutionEntity entity) {
        return Institution.builder().id(entity.getId())
              .school(entity.getSchool())
              .build();
    }

    @Override
    protected void apply(Institution model, InstitutionEntity entity) {
        entity.setSchool(model.getSchool());
    }
}
