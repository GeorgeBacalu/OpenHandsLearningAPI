package com.project.ohl.adapter.persistence.institution;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.domain.model.Institution;
import com.project.ohl.application.port.out.InstitutionRepositoryPort;
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
    protected Long idOf(Institution model) {
        return model.getId();
    }

    @Override
    protected Institution toDomain(InstitutionEntity entity) {
        return Institution.builder().id(entity.getId())
              .school(entity.getSchool())
              .classroom(entity.getClassroom())
              .build();
    }

    @Override
    protected void apply(Institution model, InstitutionEntity entity) {
        entity.setSchool(model.getSchool());
        entity.setClassroom(model.getClassroom());
    }
}
