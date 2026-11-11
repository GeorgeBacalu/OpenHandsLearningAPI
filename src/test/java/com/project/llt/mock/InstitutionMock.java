package com.project.llt.mock;

import com.project.llt.institution.Institution;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InstitutionMock {

    public static List<Institution> getMockedInstitutions() {
        return List.of(getMockedInstitution1(), getMockedInstitution2());
    }

    public static Institution getMockedInstitution1() {
        return Institution.builder()
              .id(1L)
              .school("test_institution_school1")
              .classroom("test_institution_classroom1")
              .build();
    }

    public static Institution getMockedInstitution2() {
        return Institution.builder()
              .id(2L)
              .school("test_institution_school2")
              .classroom("test_institution_classroom2")
              .build();
    }
}
