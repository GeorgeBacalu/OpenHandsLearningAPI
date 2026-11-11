package com.project.llt.mock;

import com.project.llt.user.User;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.llt.mock.InstitutionMock.getMockedInstitution1;
import static com.project.llt.mock.InstitutionMock.getMockedInstitution2;
import static com.project.llt.mock.RoleMock.getMockedRole1;
import static com.project.llt.mock.RoleMock.getMockedRole2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMock {

    public static List<User> getMockedUsers() {
        return List.of(getMockedUser1(), getMockedUser2());
    }

    public static User getMockedUser1() {
        return User.builder()
              .id(1L)
              .name("test_user_name1")
              .email("test_user_email1@email.com")
              .password("test_user_password1")
              .mobile("0000000001")
              .address("test_user_address1")
              .birthday(LocalDate.of(2000, 1, 1))
              .institution(getMockedInstitution1())
              .role(getMockedRole1())
              .build();
    }

    public static User getMockedUser2() {
        return User.builder()
              .id(2L)
              .name("test_user_name2")
              .email("test_user_email2@email.com")
              .password("test_user_password2")
              .mobile("0000000002")
              .address("test_user_address2")
              .birthday(LocalDate.of(2000, 1, 2))
              .institution(getMockedInstitution2())
              .role(getMockedRole2())
              .build();
    }
}
