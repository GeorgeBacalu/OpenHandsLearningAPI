package com.project.ohl.adapter.web;

import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.port.in.CategoryService;

import jakarta.validation.Valid;
import java.util.List;
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
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoryDto> saveCategory(@RequestBody @Valid CategoryDto categoryDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.saveCategory(categoryDto));
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoryDto> updateCategoryById(@RequestBody @Valid CategoryDto categoryDto, @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.updateCategoryById(categoryDto, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<CategoryDto>> getCategoriesByParentIdAndSectionIdAndName(@RequestParam(required = false) Long parentId,
                                                                                        @RequestParam(required = false, defaultValue = "1") Long sectionId,
                                                                                        @RequestParam(required = false, defaultValue = "") String name) {
        return ResponseEntity.ok(categoryService.getCategoriesByParentIdAndSectionIdAndName(parentId, sectionId, name));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<CategoryDto>> getFavoriteCategories() {
        return ResponseEntity.ok(categoryService.getFavoriteCategories());
    }

    @GetMapping("/favorites/filter")
    public ResponseEntity<List<CategoryDto>> getFavoritesByName(@RequestParam String name) {
        return ResponseEntity.ok(categoryService.getFavoritesByName(name));
    }

    @PatchMapping("/favorites/save")
    public ResponseEntity<CategoryDto> saveFavorite(@RequestParam Long categoryId) {
        return ResponseEntity.ok(categoryService.saveFavorite(categoryId));
    }

    @PatchMapping("/favorites/delete")
    public ResponseEntity<CategoryDto> deleteFavorite(@RequestParam Long categoryId) {
        return ResponseEntity.ok(categoryService.deleteFavorite(categoryId));
    }
}
