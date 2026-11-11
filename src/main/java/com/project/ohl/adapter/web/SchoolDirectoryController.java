package com.project.ohl.adapter.web;

import com.project.ohl.application.port.out.SchoolDirectoryPort;
import com.project.ohl.domain.model.SchoolDirectory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SchoolDirectoryController {
    private final SchoolDirectoryPort directory;

    @GetMapping("/api/counties")
    public List<SchoolDirectory.County> counties() {
        return directory.counties();
    }

    @GetMapping("/api/cities")
    public List<SchoolDirectory.City> cities(@RequestParam UUID countyId, @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "50") int limit) {
        return directory.cities(countyId, name, offset, limit);
    }

    @GetMapping("/api/schools/search")
    public List<SchoolDirectory.School> schools(@RequestParam UUID cityId, @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "50") int limit) {
        return directory.schools(cityId, name, offset, limit);
    }
}
