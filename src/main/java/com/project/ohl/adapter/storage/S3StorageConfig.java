package com.project.ohl.adapter.storage;

import java.time.Duration;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration(proxyBeanMethods = false)
public class S3StorageConfig {
    @Bean
    AwsCredentialsProvider s3Credentials(
            @Value("${app.storage.s3.endpoint:}") String endpoint,
            @Value("${app.storage.s3.access-key}") String accessKey,
            @Value("${app.storage.s3.secret-key}") String secretKey) {
        if (accessKey.isBlank() && secretKey.isBlank()) {
            if (!endpoint.isBlank()) {
                throw new IllegalArgumentException("Explicit credentials are required for a custom media storage endpoint.");
            }
            return DefaultCredentialsProvider.builder().build();
        }
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    @Bean(destroyMethod = "close")
    S3Client s3Client(@Value("${app.storage.s3.region}") String region,
            @Value("${app.storage.s3.endpoint:}") String endpoint, AwsCredentialsProvider s3Credentials) {
        var builder = S3Client.builder().region(Region.of(region)).credentialsProvider(s3Credentials)
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(60)))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofMinutes(3)).apiCallAttemptTimeout(Duration.ofMinutes(1)).build());
        if (!endpoint.isBlank()) {
            URI uri = URI.create(endpoint);
            if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null
                    || uri.getQuery() != null || uri.getFragment() != null
                    || !(uri.getPath().isEmpty() || uri.getPath().equals("/"))) {
                throw new IllegalArgumentException("Media storage endpoint must be an HTTPS origin.");
            }
            builder.endpointOverride(uri).forcePathStyle(true);
        }
        return builder.build();
    }

}
