package com.project.ohl.adapter.persistence.school;

import com.project.ohl.adapter.persistence.PersistentEntity;
import com.project.ohl.adapter.persistence.directory.CityEntity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "schools")
@Check(constraints = "trim(name) <> ''")
public class SchoolEntity extends PersistentEntity {
    @Column(name = "name", nullable = false)
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity city;
}
