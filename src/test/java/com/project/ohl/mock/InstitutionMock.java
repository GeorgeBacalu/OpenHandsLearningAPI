package com.project.ohl.mock;

import static com.project.ohl.TestIds.id;
import com.project.ohl.domain.model.Institution;
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
              .id(id(1))
              .school("test_institution_school1")
              .classroom("test_institution_classroom1")
              .build();
    }

    public static Institution getMockedInstitution2() {
        return Institution.builder()
              .id(id(2))
              .school("test_institution_school2")
              .classroom("test_institution_classroom2")
              .build();
    }
}
