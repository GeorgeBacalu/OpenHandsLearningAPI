package com.project.llt.institution;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface InstitutionApi {

    @Operation(summary = "Get all institutions", description = "Return a list of institutions", tags = {"institution"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "No institutions found"),
          @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<List<InstitutionDto>> getAllInstitutions();

    @Operation(summary = "Get institution by ID", description = "Return the institution with the given ID", tags = {"institution"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of institution to retrieve", example = "1"))
    ResponseEntity<InstitutionDto> getInstitutionById(Long id);

    @Operation(summary = "Save institution", description = "Create a new institution", tags = {"institution"}, responses = {
          @ApiResponse(responseCode = "201", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "institution object to save"))
    ResponseEntity<InstitutionDto> saveInstitution(InstitutionDto institutionDto);

    @Operation(summary = "Update institution by ID", description = "Update an existing institution by ID", tags = {"institution"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "institution object to update"),
          parameters = @Parameter(name = "id", description = "ID of institution to update", example = "1"))
    ResponseEntity<InstitutionDto> updateInstitutionById(InstitutionDto institutionDto, Long id);

    @Operation(summary = "Delete institution by ID", description = "Delete an existing institution by ID", tags = {"institution"}, responses = {
          @ApiResponse(responseCode = "204", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of institution to delete", example = "1"))
    ResponseEntity<Void> deleteInstitutionById(Long id);
}
