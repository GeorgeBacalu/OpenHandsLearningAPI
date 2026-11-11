package com.project.llt.feedback;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface FeedbackApi {

    @Operation(summary = "Get all feedbacks", description = "Return a list of feedbacks", tags = {"feedback"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "No feedbacks found"),
          @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<List<FeedbackDto>> getAllFeedbacks();

    @Operation(summary = "Get feedback by ID", description = "Return the feedback with the given ID", tags = {"feedback"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of feedback to retrieve", example = "1"))
    ResponseEntity<FeedbackDto> getFeedbackById(Long id);

    @Operation(summary = "Save feedback", description = "Create a new feedback", tags = {"feedback"}, responses = {
          @ApiResponse(responseCode = "201", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "feedback object to save"))
    ResponseEntity<FeedbackDto> saveFeedback(FeedbackDto feedbackDto);

    @Operation(summary = "Update feedback by ID", description = "Update an existing feedback by ID", tags = {"feedback"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "feedback object to update"),
          parameters = @Parameter(name = "id", description = "ID of feedback to update", example = "1"))
    ResponseEntity<FeedbackDto> updateFeedbackById(FeedbackDto feedbackDto, Long id);

    @Operation(summary = "Delete feedback by ID", description = "Delete an existing feedback by ID", tags = {"feedback"}, responses = {
          @ApiResponse(responseCode = "204", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of feedback to delete", example = "1"))
    ResponseEntity<Void> deleteFeedbackById(Long id);
}
