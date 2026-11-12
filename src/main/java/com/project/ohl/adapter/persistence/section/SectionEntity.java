package com.project.ohl.adapter.persistence.section;

import com.project.ohl.adapter.persistence.PersistentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sections")
@Check(constraints = "trim(name) <> ''")
public class SectionEntity extends PersistentEntity {
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "media_key", nullable = false)
    private String mediaKey;
    @Column(name = "icon_key", nullable = false)
    private String iconKey;
}
