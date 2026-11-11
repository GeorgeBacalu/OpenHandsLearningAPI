package com.project.ohl.adapter.persistence.classroom;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.adapter.persistence.institution.InstitutionEntity;
import com.project.ohl.application.port.out.ClassroomRepositoryPort;
import com.project.ohl.domain.model.Classroom;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ClassroomPersistenceAdapter extends JpaPersistenceAdapter<Classroom, ClassroomEntity> implements ClassroomRepositoryPort {
    public ClassroomPersistenceAdapter(ClassroomJpaRepository repository) { super(repository); }
    @Override protected ClassroomEntity newEntity() { return new ClassroomEntity(); }
    @Override protected UUID idOf(Classroom model) { return model.getId(); }
    @Override protected Classroom toDomain(ClassroomEntity entity) {
        return Classroom.builder().id(entity.getId()).name(entity.getName()).institutionId(entity.getInstitution().getId()).build();
    }
    @Override protected void apply(Classroom model, ClassroomEntity entity) {
        entity.setName(model.getName());
        entity.setInstitution(reference(InstitutionEntity.class, model.getInstitutionId()));
    }
}
