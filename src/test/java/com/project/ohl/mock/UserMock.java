package com.project.ohl.mock;

import com.project.ohl.domain.model.User;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.ohl.TestIds.id;
import static com.project.ohl.mock.SchoolMock.getMockedSchool1;
import static com.project.ohl.mock.SchoolMock.getMockedSchool2;
import static com.project.ohl.mock.RoleMock.getMockedRole1;
import static com.project.ohl.mock.RoleMock.getMockedRole2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMock {

    public static List<User> getMockedUsers() {
        return List.of(getMockedUser1(), getMockedUser2());
    }

    public static User getMockedUser1() {
        return User.builder()
              .id(id(1))
              .name("test_user_name1")
              .email("test_user_email1@email.com")
              .password("test_user_password1")
              .phone("0000000001")
              .school(getMockedSchool1())
              .role(getMockedRole1())
              .build();
    }

    public static User getMockedUser2() {
        return User.builder()
              .id(id(2))
              .name("test_user_name2")
              .email("test_user_email2@email.com")
              .password("test_user_password2")
              .phone("0000000002")
              .school(getMockedSchool2())
              .role(getMockedRole2())
              .build();
    }
}
