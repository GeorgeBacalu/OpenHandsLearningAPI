package com.project.ohl.adapter.persistence.institution;

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
@Table(name = "institutions")
@Check(constraints = "trim(school) <> ''")
public class InstitutionEntity extends PersistentEntity {
    @Column(name = "school", nullable = false, unique = true)
    private String school;
}
