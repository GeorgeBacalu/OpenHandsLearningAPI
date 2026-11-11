package com.project.ohl.mock;

import com.project.ohl.domain.model.School;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.ohl.TestIds.id;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchoolMock {

    public static List<School> getMockedSchools() {
        return List.of(getMockedSchool1(), getMockedSchool2());
    }

    public static School getMockedSchool1() {
        return School.builder()
              .id(id(1))
              .name("test_school_school1").cityId(id(104))
              .build();
    }

    public static School getMockedSchool2() {
        return School.builder()
              .id(id(2))
              .name("test_school_school2").cityId(id(105))
              .build();
    }
}
