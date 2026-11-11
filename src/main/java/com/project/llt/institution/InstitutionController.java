package com.project.llt.institution;

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
@RequestMapping(value = "/api/institutions", produces = APPLICATION_JSON_VALUE)
public class InstitutionController implements InstitutionApi {

    private final InstitutionService institutionService;

    @Override @GetMapping
    public ResponseEntity<List<InstitutionDto>> getAllInstitutions() {
        return ResponseEntity.ok(institutionService.getAllInstitutions());
    }

    @Override @GetMapping("/{id}")
    public ResponseEntity<InstitutionDto> getInstitutionById(@PathVariable Long id) {
        return ResponseEntity.ok(institutionService.getInstitutionById(id));
    }

    @Override @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<InstitutionDto> saveInstitution(@RequestBody @Valid InstitutionDto institutionDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(institutionService.saveInstitution(institutionDto));
    }

    @Override @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<InstitutionDto> updateInstitutionById(@RequestBody @Valid InstitutionDto institutionDto, @PathVariable Long id) {
        return ResponseEntity.ok(institutionService.updateInstitutionById(institutionDto, id));
    }

    @Override @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstitutionById(@PathVariable Long id) {
        institutionService.deleteInstitutionById(id);
        return ResponseEntity.noContent().build();
    }
}
