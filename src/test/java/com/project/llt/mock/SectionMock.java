package com.project.llt.mock;

import com.project.llt.section.Section;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SectionMock {

    public static List<Section> getMockedSections() {
        return List.of(getMockedSection1(), getMockedSection2());
    }

    public static Section getMockedSection1() {
        return Section.builder()
              .id(1L)
              .name("test_section_name1")
              .iconId(1)
              .imageId(1)
              .build();
    }

    public static Section getMockedSection2() {
        return Section.builder()
              .id(2L)
              .name("test_section_name2")
              .iconId(2)
              .imageId(2)
              .build();
    }
}
