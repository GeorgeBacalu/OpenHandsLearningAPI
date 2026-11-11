package com.project.ohl.adapter.persistence.institution;

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
@Table(name = "institutions")
public class InstitutionEntity extends PersistentEntity {
    @Column(name = "school")
    private String school;
    @Column(name = "classroom")
    private String classroom;
}
