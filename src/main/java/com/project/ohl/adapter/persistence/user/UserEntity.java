package com.project.ohl.adapter.persistence.user;

import com.project.ohl.adapter.persistence.PersistentEntity;
import jakarta.persistence.Column;
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
@Table(name = "users")
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
    @Column(name = "institution_id")
    private Long institutionId;
    @Column(name = "role_id")
    private Long roleId;
}
