package com.project.llt.role;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface RoleApi {

    @Operation(summary = "Get all roles", description = "Return a list of roles", tags = {"role"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "No roles found"),
          @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<List<RoleDto>> getAllRoles();

    @Operation(summary = "Get role by ID", description = "Return the role with the given ID", tags = {"role"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of role to retrieve", example = "1"))
    ResponseEntity<RoleDto> getRoleById(Long id);

    @Operation(summary = "Save role", description = "Create a new role", tags = {"role"}, responses = {
          @ApiResponse(responseCode = "201", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "role object to save"))
    ResponseEntity<RoleDto> saveRole(RoleDto roleDto);

    @Operation(summary = "Update role by ID", description = "Update an existing role by ID", tags = {"role"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "role object to update"),
          parameters = @Parameter(name = "id", description = "ID of role to update", example = "1"))
    ResponseEntity<RoleDto> updateRoleById(RoleDto roleDto, Long id);

    @Operation(summary = "Delete role by ID", description = "Delete an existing role by ID", tags = {"role"}, responses = {
          @ApiResponse(responseCode = "204", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of role to delete", example = "1"))
    ResponseEntity<Void> deleteRoleById(Long id);
}
