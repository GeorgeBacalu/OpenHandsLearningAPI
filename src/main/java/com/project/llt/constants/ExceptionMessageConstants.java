package com.project.llt.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionMessageConstants {

    public static final String RESOURCE_NOT_FOUND = "Resource not found: %s";
    public static final String INVALID_REQUEST = "Invalid request: %s";
    public static final String HTTP_REQUEST_METHOD_NOT_SUPPORTED = "Http request method not supported: %s";
    public static final String HTTP_MEDIA_TYPE_NOT_SUPPORTED = "Http media type not supported: %s";
    public static final String METHOD_ARGUMENT_NOT_VALID = "Method argument not valid: %s";
    public static final String DATA_ACCESS_VIOLATION = "Data access violation: %s";

    public static final String CATEGORY_NOT_FOUND = "Category with id %s was not found";
    public static final String FEEDBACK_NOT_FOUND = "Feedback with id %s was not found";
    public static final String INSTITUTION_NOT_FOUND = "Institution with id %s was not found";
    public static final String LETTER_SIGN_PAIR_NOT_FOUND = "Letter-sign pair with id %s was not found";
    public static final String NOTIFICATION_NOT_FOUND = "Notification with id %s was not found";
    public static final String ROLE_NOT_FOUND = "Role with id %s was not found";
    public static final String SECTION_NOT_FOUND = "Section with id %s was not found";
    public static final String USER_NOT_FOUND = "User with id %s was not found";
}
