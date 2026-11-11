package com.project.ohl.mock;

import com.project.ohl.domain.model.Institution;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.ohl.TestIds.id;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InstitutionMock {

    public static List<Institution> getMockedInstitutions() {
        return List.of(getMockedInstitution1(), getMockedInstitution2());
    }

    public static Institution getMockedInstitution1() {
        return Institution.builder()
              .id(id(1))
              .school("test_institution_school1")
              .build();
    }

    public static Institution getMockedInstitution2() {
        return Institution.builder()
              .id(id(2))
              .school("test_institution_school2")
              .build();
    }
}
