package com.project.ohl.adapter.persistence.section;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.application.port.out.SectionRepositoryPort;
import com.project.ohl.domain.model.CatalogMetadata;
import com.project.ohl.domain.model.Section;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SectionPersistenceAdapter extends JpaPersistenceAdapter<Section, SectionEntity> implements SectionRepositoryPort {

    public SectionPersistenceAdapter(SectionJpaRepository repository) {
        super(repository);
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
    public Section toDomain(SectionEntity entity) {
        return Section.builder().id(entity.getId())
              .name(entity.getName()).mediaKey(entity.getMediaKey()).iconKey(entity.getIconKey())

              .build();
    }

    @Override
    protected void apply(Section model, SectionEntity entity) {
        entity.setName(model.getName());
        entity.setMediaKey(CatalogMetadata.mediaKey(model.getMediaKey(), entity.getMediaKey(), model.getName()));
        entity.setIconKey(CatalogMetadata.mediaKey(model.getIconKey(), entity.getIconKey(), "ic_" + model.getName()));

    }
}
