package com.project.ohl.adapter.web;

import java.util.UUID;
import com.project.ohl.application.model.SectionDto;
import com.project.ohl.application.port.in.SectionService;

import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping(value = "/api/sections", produces = APPLICATION_JSON_VALUE)
public class SectionController {

    private final SectionService sectionService;

    @GetMapping
    public ResponseEntity<List<SectionDto>> getAllSections() {
        return ResponseEntity.ok(sectionService.getAllSections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionDto> getSectionById(@PathVariable UUID id) {
        return ResponseEntity.ok(sectionService.getSectionById(id));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<SectionDto> saveSection(@RequestBody @Valid SectionDto sectionDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.saveSection(sectionDto));
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<SectionDto> updateSectionById(@RequestBody @Valid SectionDto sectionDto, @PathVariable UUID id) {
        return ResponseEntity.ok(sectionService.updateSectionById(sectionDto, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSectionById(@PathVariable UUID id) {
        sectionService.deleteSectionById(id);
        return ResponseEntity.noContent().build();
    }
}
