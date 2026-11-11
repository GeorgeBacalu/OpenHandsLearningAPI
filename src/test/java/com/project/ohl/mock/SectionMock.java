package com.project.ohl.mock;

import com.project.ohl.domain.model.Section;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.ohl.TestIds.id;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SectionMock {

    public static List<Section> getMockedSections() {
        return List.of(getMockedSection1(), getMockedSection2());
    }

    public static Section getMockedSection1() {
        return Section.builder()
              .id(id(1))
              .name("test_section_name1")
              .iconId(1)
              .imageId(1)
              .build();
    }

    public static Section getMockedSection2() {
        return Section.builder()
              .id(id(2))
              .name("test_section_name2")
              .iconId(2)
              .imageId(2)
              .build();
    }
}
