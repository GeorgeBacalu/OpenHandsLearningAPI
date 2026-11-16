package com.project.ohl.adapter.storage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.storage.s3")
public record S3StorageProperties(
        @DefaultValue("us-east-1") @NotBlank @Pattern(regexp = "[a-z]{2}(-[a-z]+)+-[0-9]+") String region,
        @DefaultValue("open-hands-learning") @NotBlank
        @Pattern(regexp = "[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]") String bucket) {}
