package com.project.ohl.adapter.persistence.role;

import com.project.ohl.adapter.persistence.PersistentEntity;
import com.project.ohl.domain.model.Authority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class RoleEntity extends PersistentEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "authority", nullable = false, unique = true)
    private Authority authority;
}
