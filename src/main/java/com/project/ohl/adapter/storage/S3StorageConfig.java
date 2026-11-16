package com.project.ohl.adapter.storage;

import java.time.Duration;
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
            @Value("${AWS_ACCESS_KEY}") String accessKey,
            @Value("${AWS_SECRET_ACCESS}") String secretKey) {
        if (accessKey.isBlank() && secretKey.isBlank()) {
            return DefaultCredentialsProvider.builder().build();
        }
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    @Bean(destroyMethod = "close")
    S3Client s3Client(@Value("${app.storage.s3.region}") String region, AwsCredentialsProvider s3Credentials) {
        return S3Client.builder().region(Region.of(region)).credentialsProvider(s3Credentials)
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(60)))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofMinutes(3)).apiCallAttemptTimeout(Duration.ofMinutes(1)).build())
                .build();
    }

}
