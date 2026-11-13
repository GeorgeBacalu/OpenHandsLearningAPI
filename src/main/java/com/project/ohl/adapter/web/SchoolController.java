package com.project.ohl.adapter.web;

import com.project.ohl.application.model.SchoolDto;
import com.project.ohl.application.port.in.SchoolService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/schools", produces = APPLICATION_JSON_VALUE)
public class SchoolController {

    private final SchoolService schoolService;

    @GetMapping
    public ResponseEntity<List<SchoolDto>> getAllSchools() {
        return ResponseEntity.ok(schoolService.getAllSchools());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolDto> getSchoolById(@PathVariable UUID id) {
        return ResponseEntity.ok(schoolService.getSchoolById(id));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<SchoolDto> saveSchool(@RequestBody @Valid SchoolDto schoolDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schoolService.saveSchool(schoolDto));
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<SchoolDto> updateSchoolById(@RequestBody @Valid SchoolDto schoolDto, @PathVariable UUID id) {
        return ResponseEntity.ok(schoolService.updateSchoolById(schoolDto, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchoolById(@PathVariable UUID id) {
        schoolService.deleteSchoolById(id);
        return ResponseEntity.noContent().build();
    }
}
