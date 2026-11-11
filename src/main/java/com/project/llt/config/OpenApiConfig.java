package com.project.llt.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
      info = @Info(
            title = "Let's Learn Together REST API",
            description = "This is a sample REST API documentation of our application that is meant to help students with hearing impairments",
            version = "1.0.0"),
      tags = {
            @Tag(name = "category", description = "Category REST Controller operations"),
            @Tag(name = "feedback", description = "Feedback REST Controller operations"),
            @Tag(name = "institution", description = "Institution REST Controller operations"),
            @Tag(name = "letter-sign-pair", description = "Letter-Sign Pair REST Controller operations"),
            @Tag(name = "notification", description = "Notification REST Controller operations"),
            @Tag(name = "role", description = "Role REST Controller operations"),
            @Tag(name = "section", description = "Section REST Controller operations"),
            @Tag(name = "user", description = "User REST Controller operations")})
public class OpenApiConfig {
}
