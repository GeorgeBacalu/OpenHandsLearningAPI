package com.project.ohl.constants;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.ohl.TestIds.id;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentifierConstants {

    public static final UUID VALID_ID = id(1);
    public static final UUID INVALID_ID = id(999);
}
