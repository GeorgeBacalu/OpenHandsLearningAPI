package com.project.ohl.adapter.web;

import com.project.ohl.application.model.LetterSignPairDto;
import com.project.ohl.application.port.in.LetterSignPairService;
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
@RequestMapping(value = "/api/letter-sign-pairs", produces = APPLICATION_JSON_VALUE)
public class LetterSignPairController {

    private final LetterSignPairService letterSignPairService;

    @GetMapping
    public ResponseEntity<List<LetterSignPairDto>> getAllLetterSignPairs() {
        return ResponseEntity.ok(letterSignPairService.getAllLetterSignPairs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LetterSignPairDto> getLetterSignPairById(@PathVariable UUID id) {
        return ResponseEntity.ok(letterSignPairService.getLetterSignPairById(id));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<LetterSignPairDto> saveLetterSignPair(@RequestBody @Valid LetterSignPairDto letterSignPairDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(letterSignPairService.saveLetterSignPair(letterSignPairDto));
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<LetterSignPairDto> updateLetterSignPairById(@RequestBody @Valid LetterSignPairDto letterSignPairDto, @PathVariable UUID id) {
        return ResponseEntity.ok(letterSignPairService.updateLetterSignPairById(letterSignPairDto, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLetterSignPairById(@PathVariable UUID id) {
        letterSignPairService.deleteLetterSignPairById(id);
        return ResponseEntity.noContent().build();
    }
}
