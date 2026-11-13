package com.project.ohl.adapter.persistence.directory;

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
@Table(name = "counties")
public class CountyEntity extends PersistentEntity {
    @Column(nullable = false, unique = true)
    private String name;
}
