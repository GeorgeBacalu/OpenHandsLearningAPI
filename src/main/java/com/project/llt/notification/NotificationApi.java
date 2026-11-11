package com.project.llt.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface NotificationApi {

    @Operation(summary = "Get all notifications", description = "Return a list of notifications", tags = {"notification"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "No notifications found"),
          @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<List<NotificationDto>> getAllNotifications();

    @Operation(summary = "Get notification by ID", description = "Return the notification with the given ID", tags = {"notification"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of notification to retrieve", example = "1"))
    ResponseEntity<NotificationDto> getNotificationById(Long id);

    @Operation(summary = "Save notification", description = "Create a new notification", tags = {"notification"}, responses = {
          @ApiResponse(responseCode = "201", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "notification object to save"))
    ResponseEntity<NotificationDto> saveNotification(NotificationDto notificationDto);

    @Operation(summary = "Update notification by ID", description = "Update an existing notification by ID", tags = {"notification"}, responses = {
          @ApiResponse(responseCode = "200", description = "Successful operation"),
          @ApiResponse(responseCode = "400", description = "Invalid request body"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          requestBody = @RequestBody(description = "notification object to update"),
          parameters = @Parameter(name = "id", description = "ID of notification to update", example = "1"))
    ResponseEntity<NotificationDto> updateNotificationById(NotificationDto notificationDto, Long id);

    @Operation(summary = "Delete notification by ID", description = "Delete an existing notification by ID", tags = {"notification"}, responses = {
          @ApiResponse(responseCode = "204", description = "Successful operation"),
          @ApiResponse(responseCode = "404", description = "Invalid ID supplied"),
          @ApiResponse(responseCode = "500", description = "Internal server error")},
          parameters = @Parameter(name = "id", description = "ID of notification to delete", example = "1"))
    ResponseEntity<Void> deleteNotificationById(Long id);
}
