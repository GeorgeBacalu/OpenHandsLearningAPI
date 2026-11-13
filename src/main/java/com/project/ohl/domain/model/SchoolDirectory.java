package com.project.ohl.domain.model;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public record SchoolDirectory(List<County> counties, List<City> cities, List<School> schools) {
    public SchoolDirectory {
        counties = List.copyOf(counties);
        cities = List.copyOf(cities);
        schools = List.copyOf(schools);
    }

    public static UUID sourceId(String kind, String code) {
        return UUID.nameUUIDFromBytes(("OpenHandsLearning:" + kind + ":" + code).getBytes(StandardCharsets.UTF_8));
    }

    public record County(UUID id, String name) {}
    public record City(UUID id, String name, UUID countyId) {}
    public record School(UUID id, String name, UUID cityId) {}
}
