package com.project.ohl.adapter.persistence.classroom;

import com.project.ohl.adapter.persistence.PersistentEntity;
import com.project.ohl.adapter.persistence.institution.InstitutionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Getter @Setter @NoArgsConstructor @Entity
@Table(name = "classrooms", uniqueConstraints = @UniqueConstraint(columnNames = {"id", "institution_id"}))
@Check(constraints = "trim(name) <> ''")
public class ClassroomEntity extends PersistentEntity {
    @Column(nullable = false) private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private InstitutionEntity institution;
}
