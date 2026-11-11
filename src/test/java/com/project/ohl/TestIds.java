package com.project.ohl;

import java.util.UUID;

public final class TestIds {
    private TestIds() {}

    public static UUID id(long value) {
        return new UUID(0x4000L, 0x8000000000000000L | value);
    }
}
