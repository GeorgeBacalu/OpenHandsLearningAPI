package com.project.ohl.mock;

import static com.project.ohl.TestIds.id;
import com.project.ohl.domain.model.LetterSignPair;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LetterSignPairMock {

    public static List<LetterSignPair> getMockedLetterSignPairs() {
        return List.of(getMockedLetterSignPair1(), getMockedLetterSignPair2());
    }

    public static LetterSignPair getMockedLetterSignPair1() {
        return LetterSignPair.builder()
              .id(id(1))
              .letter("a")
              .imageId(1)
              .build();
    }

    public static LetterSignPair getMockedLetterSignPair2() {
        return LetterSignPair.builder()
              .id(id(2))
              .letter("b")
              .imageId(2)
              .build();
    }
}
