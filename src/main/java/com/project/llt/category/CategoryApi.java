package com.project.llt.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface CategoryApi {

    @Operation(summary = "Get all categories", description = "Return a list of categories", tags = {"category"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "No categories found"),
          @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<List<CategoryDto>> getAllCategories();

    @Operation(summary = "Get category by ID", description = "Return the category with the given ID", tags = {"category"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of category to retrieve", example = "1"))
    ResponseEntity<CategoryDto> getCategoryById(Long id);

    @Operation(summary = "Save category", description = "Create a new category", tags = {"category"}, responses = {
          @ApiResponse(responseCode = "201", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "Category object to save"))
    ResponseEntity<CategoryDto> saveCategory(CategoryDto categoryDto);

    @Operation(summary = "Update category by ID", description = "Update an existing category by ID", tags = {"category"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "Category object to update"),
          parameters = @Parameter(name = "id", description = "ID of category to update", example = "1"))
    ResponseEntity<CategoryDto> updateCategoryById(CategoryDto categoryDto, Long id);

    @Operation(summary = "Delete category by ID", description = "Delete an existing category by ID", tags = {"category"}, responses = {
          @ApiResponse(responseCode = "204", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of category to delete", example = "1"))
    ResponseEntity<Void> deleteCategoryById(Long id);

    @Operation(summary = "Get categories by parent and section ID and filters by name", description = "Return list of categories by parent and section ID filtered by name", tags = {"category"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = {
                @Parameter(name = "parentId", description = "ID of parent category the filter will be applied to", example = "1"),
                @Parameter(name = "sectionId", description = "ID of section of the category", example = "1"),
                @Parameter(name = "name", description = "Name of category to filter by", example = "a")
          })
    ResponseEntity<List<CategoryDto>> getCategoriesByParentIdAndSectionIdAndName(Long parentId, Long sectionId, String name);

    @Operation(summary = "Get all favorite categories", description = "Return a list of favorite categories", tags = {"category"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "No categories found"),
          @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<List<CategoryDto>> getFavoriteCategories();

    @Operation(summary = "Get filtered favorites by name", description = "Return a list of filtered favorites by name", tags = {"category"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "name", description = "Name filter for favorites", example = "a"))
    ResponseEntity<List<CategoryDto>> getFavoritesByName(String name);

    @Operation(summary = "Save category to favorites", description = "Save the category with the given ID to favorites", tags = {"category"}, responses = {
          @ApiResponse(responseCode = "204", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "categoryId", description = "ID of category to save to favorites", example = "1"))
    ResponseEntity<CategoryDto> saveFavorite(Long categoryId);

    @Operation(summary = "Delete category from favorites", description = "Delete the category with the given ID from favorites", tags = {"category"}, responses = {
          @ApiResponse(responseCode = "204", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "categoryId", description = "ID of category to delete from favorites", example = "1"))
    ResponseEntity<CategoryDto> deleteFavorite(Long categoryId);
}
