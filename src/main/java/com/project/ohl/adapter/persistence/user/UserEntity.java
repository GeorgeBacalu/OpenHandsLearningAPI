package com.project.ohl.adapter.persistence.user;

import com.project.ohl.adapter.persistence.PersistentEntity;
import com.project.ohl.adapter.persistence.role.RoleEntity;
import com.project.ohl.adapter.persistence.institution.InstitutionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users", indexes = {
      @Index(name = "idx_users_institution", columnList = "institution_id"),
      @Index(name = "idx_users_role", columnList = "role_id")
})
public class UserEntity extends PersistentEntity {
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "mobile")
    private String mobile;
    @Column(name = "address")
    private String address;
    @Column(name = "birthday")
    private LocalDate birthday;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    private InstitutionEntity institution;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private RoleEntity role;
}
