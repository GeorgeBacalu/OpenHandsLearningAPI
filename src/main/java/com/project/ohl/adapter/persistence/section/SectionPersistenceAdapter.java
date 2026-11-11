package com.project.ohl.adapter.persistence.section;

import org.springframework.data.domain.Sort;
import java.util.UUID;
import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.domain.model.Section;
import com.project.ohl.application.port.out.SectionRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class SectionPersistenceAdapter extends JpaPersistenceAdapter<Section, SectionEntity> implements SectionRepositoryPort {

    public SectionPersistenceAdapter(SectionJpaRepository repository) {
        super(repository);
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by("displayOrder", "id");
    }

    @Override
    protected SectionEntity newEntity() {
        return new SectionEntity();
    }

    @Override
    protected UUID idOf(Section model) {
        return model.getId();
    }

    @Override
    protected Section toDomain(SectionEntity entity) {
        return Section.builder().id(entity.getId())
              .name(entity.getName())
              .iconId(entity.getIconId())
              .imageId(entity.getImageId())
              .build();
    }

    @Override
    protected void apply(Section model, SectionEntity entity) {
        entity.setName(model.getName());
        entity.setIconId(model.getIconId());
        entity.setImageId(model.getImageId());
    }
}
