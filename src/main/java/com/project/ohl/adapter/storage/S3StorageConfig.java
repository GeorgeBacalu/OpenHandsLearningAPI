package com.project.ohl.adapter.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3StorageConfig {
    @Bean
    AwsCredentialsProvider s3Credentials(
            @Value("${app.storage.s3.access-key:${AWS_ACCESS_KEY:${AWS_ACCESS_KEY_ID:}}}") String accessKey,
            @Value("${app.storage.s3.secret-key:${AWS_SECRET_ACCESS:${AWS_SECRET_ACCESS_KEY:}}}") String secretKey) {
        if (accessKey.isBlank() && secretKey.isBlank()) {
            return DefaultCredentialsProvider.builder().build();
        }
        if (accessKey.isBlank() || secretKey.isBlank()) {
            throw new IllegalArgumentException("Configure both AWS_ACCESS_KEY and AWS_SECRET_ACCESS, or use the AWS default credential chain.");
        }
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    @Bean(destroyMethod = "close")
    S3Client s3Client(S3StorageProperties properties, AwsCredentialsProvider s3Credentials) {
        return S3Client.builder().region(Region.of(properties.region())).credentialsProvider(s3Credentials)
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(60)))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofMinutes(3)).apiCallAttemptTimeout(Duration.ofMinutes(1)).build())
                .build();
    }

}
