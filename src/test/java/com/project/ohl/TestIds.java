package com.project.ohl;

import java.util.UUID;

public final class TestIds {
    public static final UUID CATEGORY_PARENT = UUID.fromString("176ccfb5-fe9b-484c-a799-310eb72a02a5");
    public static final UUID CATEGORY_CHILD = UUID.fromString("f7198e8f-3cd4-4fe5-bc7e-8f0a39fcf185");
    public static final UUID SECTION_VIDEO = UUID.fromString("7bfc50e0-5106-495b-b08a-5c04b8f5c6a2");
    public static final UUID INSTITUTION_A = UUID.fromString("e882d373-7f2f-44b2-8c87-530cd04964d4");
    public static final UUID ROLE_USER = UUID.fromString("99d0dd27-ea57-42b3-8fd8-64e3973a05d3");

    private TestIds() {}

    public static UUID id(long value) {
        return new UUID(0x4000L, 0x8000000000000000L | value);
    }
}
