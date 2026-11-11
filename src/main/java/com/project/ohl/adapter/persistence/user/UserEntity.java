package com.project.ohl.adapter.persistence.user;

import com.project.ohl.adapter.persistence.PersistentEntity;
import com.project.ohl.adapter.persistence.classroom.ClassroomEntity;
import com.project.ohl.adapter.persistence.school.SchoolEntity;
import com.project.ohl.adapter.persistence.role.RoleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users", indexes = {
      @Index(name = "idx_users_school", columnList = "school_id"),
      @Index(name = "idx_users_role", columnList = "role_id")
})
@Check(constraints = "trim(name) <> '' AND trim(email) <> '' AND trim(password) <> '' AND trim(mobile) <> '' AND trim(address) <> ''")
public class UserEntity extends PersistentEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private ClassroomEntity classroom;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "mobile", nullable = false)
    private String mobile;
    @Column(name = "address", nullable = false)
    private String address;
    @Column(name = "birthday")
    private LocalDate birthday;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private SchoolEntity school;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;
}
