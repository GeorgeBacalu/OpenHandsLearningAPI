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
@Check(constraints = "trim(name) <> '' AND display_order >= 0")
public class SectionEntity extends PersistentEntity {
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = Integer.MAX_VALUE;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "code", nullable = false, unique = true, length = 120)
    private String code;
    @Column(name = "media_key")
    private String mediaKey;
    @Column(name = "icon_key")
    private String iconKey;
    @Column(name = "icon_id")
    private Integer iconId;
    @Column(name = "image_id")
    private Integer imageId;
}
