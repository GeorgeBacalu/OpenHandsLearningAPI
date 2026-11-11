package com.project.ohl.adapter.persistence.section;

import com.project.ohl.adapter.persistence.PersistentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sections")
public class SectionEntity extends PersistentEntity {
    @Column(name = "name")
    private String name;
    @Column(name = "icon_id")
    private Integer iconId;
    @Column(name = "image_id")
    private Integer imageId;
}
