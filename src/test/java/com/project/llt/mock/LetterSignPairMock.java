package com.project.llt.mock;

import com.project.llt.letter_sign_pair.LetterSignPair;
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
              .id(1L)
              .letter("a")
              .imageId(1)
              .build();
    }

    public static LetterSignPair getMockedLetterSignPair2() {
        return LetterSignPair.builder()
              .id(2L)
              .letter("b")
              .imageId(2)
              .build();
    }
}
