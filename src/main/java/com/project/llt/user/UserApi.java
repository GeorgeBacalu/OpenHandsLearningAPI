package com.project.llt.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface UserApi {

    @Operation(summary = "Get all users", description = "Return a list of users", tags = {"user"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "No users found"),
          @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<List<UserDto>> getAllUsers();

    @Operation(summary = "Get user by ID", description = "Return the user with the given ID", tags = {"user"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of user to retrieve", example = "1"))
    ResponseEntity<UserDto> getUserById(Long id);

    @Operation(summary = "Save user", description = "Create a new user", tags = {"user"}, responses = {
          @ApiResponse(responseCode = "201", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "user object to save"))
    ResponseEntity<UserDto> saveUser(UserDto userDto);

    @Operation(summary = "Update user by ID", description = "Update an existing user by ID", tags = {"user"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "user object to update"),
          parameters = @Parameter(name = "id", description = "ID of user to update", example = "1"))
    ResponseEntity<UserDto> updateUserById(UserDto userDto, Long id);

    @Operation(summary = "Delete user by ID", description = "Delete an existing user by ID", tags = {"user"}, responses = {
          @ApiResponse(responseCode = "204", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of user to delete", example = "1"))
    ResponseEntity<Void> deleteUserById(Long id);
}
