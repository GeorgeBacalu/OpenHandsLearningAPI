package com.project.ohl.adapter.persistence.user;

import com.project.ohl.adapter.persistence.PersistentEntity;
import com.project.ohl.adapter.persistence.school.SchoolEntity;
import com.project.ohl.domain.model.Role;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users", indexes = @Index(name = "idx_users_school", columnList = "school_id"), check = @CheckConstraint(constraint = "trim(name) <> '' AND trim(email) <> '' AND trim(password_hash) <> '' AND trim(phone) <> ''"))
public class UserEntity extends PersistentEntity {
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "phone", nullable = false)
    private String phone;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private SchoolEntity school;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 5)
    private Role role;
}
