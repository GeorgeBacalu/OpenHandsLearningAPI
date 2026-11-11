package com.project.ohl.mock;

import com.project.ohl.domain.model.Authority;
import com.project.ohl.domain.model.Role;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.ohl.TestIds.id;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RoleMock {

    public static List<Role> getMockedRoles() {
        return List.of(getMockedRole1(), getMockedRole2());
    }

    public static Role getMockedRole1() {
        return Role.builder().id(id(1)).authority(Authority.USER).build();
    }

    public static Role getMockedRole2() {
        return Role.builder().id(id(2)).authority(Authority.ADMIN).build();
    }
}
