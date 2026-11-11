package com.project.ohl.adapter.web;

import com.project.ohl.application.model.ClassroomDto;
import com.project.ohl.application.port.in.ClassroomService;
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
@RequestMapping(value = "/api/classrooms", produces = APPLICATION_JSON_VALUE)
public class ClassroomController {

    private final ClassroomService classroomService;

    @GetMapping
    public ResponseEntity<List<ClassroomDto>> getAllClassrooms() {
        return ResponseEntity.ok(classroomService.getAllClassrooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomDto> getClassroomById(@PathVariable UUID id) {
        return ResponseEntity.ok(classroomService.getClassroomById(id));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<ClassroomDto> saveClassroom(@RequestBody @Valid ClassroomDto classroomDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classroomService.saveClassroom(classroomDto));
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<ClassroomDto> updateClassroomById(@RequestBody @Valid ClassroomDto classroomDto, @PathVariable UUID id) {
        return ResponseEntity.ok(classroomService.updateClassroomById(classroomDto, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassroomById(@PathVariable UUID id) {
        classroomService.deleteClassroomById(id);
        return ResponseEntity.noContent().build();
    }
}
