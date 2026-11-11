package com.project.llt.letter_sign_pair;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface LetterSignPairApi {

    @Operation(summary = "Get all letter-sign pairs", description = "Return a list of letter-sign pairs", tags = {"letter-sign-pair"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "No letter-sign pairs found"),
          @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<List<LetterSignPairDto>> getAllLetterSignPairs();

    @Operation(summary = "Get letter-sign pair by ID", description = "Return the letter-sign pair with the given ID", tags = {"letter-sign-pair"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of letter-sign pair to retrieve", example = "1"))
    ResponseEntity<LetterSignPairDto> getLetterSignPairById(Long id);

    @Operation(summary = "Save letter-sign pair", description = "Create a new letter-sign pair", tags = {"letter-sign-pair"}, responses = {
          @ApiResponse(responseCode = "201", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "letter-sign pair object to save"))
    ResponseEntity<LetterSignPairDto> saveLetterSignPair(LetterSignPairDto letterSignPairDto);

    @Operation(summary = "Update letter-sign pair by ID", description = "Update an existing letter-sign pair by ID", tags = {"letter-sign-pair"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "letter-sign pair object to update"),
          parameters = @Parameter(name = "id", description = "ID of letter-sign pair to update", example = "1"))
    ResponseEntity<LetterSignPairDto> updateLetterSignPairById(LetterSignPairDto letterSignPairDto, Long id);

    @Operation(summary = "Delete letter-sign pair by ID", description = "Delete an existing letter-sign pair by ID", tags = {"letter-sign-pair"}, responses = {
          @ApiResponse(responseCode = "204", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of letter-sign pair to delete", example = "1"))
    ResponseEntity<Void> deleteLetterSignPairById(Long id);
}
