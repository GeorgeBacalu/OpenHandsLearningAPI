package com.project.ohl.adapter.web;

import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.port.in.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/categories", produces = APPLICATION_JSON_VALUE)
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable UUID id, @RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(userId == null ? categoryService.getCategoryById(id) : categoryService.getCategoryById(id, userId));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoryDto> saveCategory(@RequestBody @Valid CategoryDto categoryDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.saveCategory(categoryDto));
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoryDto> updateCategoryById(@RequestBody @Valid CategoryDto categoryDto, @PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.updateCategoryById(categoryDto, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable UUID id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<CategoryDto>> getCategoriesByParentIdAndSectionIdAndName(@RequestParam(required = false) UUID parentId,
            @RequestParam(required = false) UUID sectionId, @RequestParam(defaultValue = "") String name,
            @RequestParam(required = false) String parentCode, @RequestParam(defaultValue = "VIDEO") String sectionCode) {
        if (parentCode == null && sectionId != null) return ResponseEntity.ok(categoryService.getCategoriesByParentIdAndSectionIdAndName(parentId, sectionId, name));
        return ResponseEntity.ok(categoryService.getCategoriesByFilters(parentId, sectionId, parentCode, sectionCode, name));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<CategoryDto>> getFavoriteCategories(@RequestParam UUID userId) {
        return ResponseEntity.ok(categoryService.getFavoriteCategories(userId));
    }

    @GetMapping("/favorites/filter")
    public ResponseEntity<List<CategoryDto>> getFavoritesByName(@RequestParam UUID userId, @RequestParam String name) {
        return ResponseEntity.ok(categoryService.getFavoritesByName(userId, name));
    }

    @PatchMapping("/favorites/save")
    public ResponseEntity<CategoryDto> saveFavorite(@RequestParam UUID userId, @RequestParam UUID categoryId) {
        return ResponseEntity.ok(categoryService.saveFavorite(userId, categoryId));
    }

    @PatchMapping("/favorites/delete")
    public ResponseEntity<CategoryDto> deleteFavorite(@RequestParam UUID userId, @RequestParam UUID categoryId) {
        return ResponseEntity.ok(categoryService.deleteFavorite(userId, categoryId));
    }
}
