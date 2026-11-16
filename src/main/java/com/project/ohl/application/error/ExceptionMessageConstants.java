package com.project.ohl.application.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionMessageConstants {

    public static final String CATEGORY_NOT_FOUND = "Category with id %s was not found";
    public static final String SCHOOL_NOT_FOUND = "School with id %s was not found";
    public static final String ROLE_NOT_FOUND = "Role with id %s was not found";
    public static final String USER_NOT_FOUND = "User with id %s was not found";
}
