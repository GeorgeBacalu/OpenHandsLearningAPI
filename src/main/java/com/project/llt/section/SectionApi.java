package com.project.llt.section;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface SectionApi {

    @Operation(summary = "Get all sections", description = "Return a list of sections", tags = {"section"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "No sections found"),
          @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<List<SectionDto>> getAllSections();

    @Operation(summary = "Get section by ID", description = "Return the section with the given ID", tags = {"section"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of section to retrieve", example = "1"))
    ResponseEntity<SectionDto> getSectionById(Long id);

    @Operation(summary = "Save section", description = "Create a new section", tags = {"section"}, responses = {
          @ApiResponse(responseCode = "201", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "section object to save"))
    ResponseEntity<SectionDto> saveSection(SectionDto sectionDto);

    @Operation(summary = "Update section by ID", description = "Update an existing section by ID", tags = {"section"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "section object to update"),
          parameters = @Parameter(name = "id", description = "ID of section to update", example = "1"))
    ResponseEntity<SectionDto> updateSectionById(SectionDto sectionDto, Long id);

    @Operation(summary = "Delete section by ID", description = "Delete an existing section by ID", tags = {"section"}, responses = {
          @ApiResponse(responseCode = "204", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of section to delete", example = "1"))
    ResponseEntity<Void> deleteSectionById(Long id);
}
