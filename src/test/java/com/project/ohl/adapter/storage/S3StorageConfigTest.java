package com.project.ohl.adapter.storage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3StorageConfigTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner().withUserConfiguration(S3StorageConfig.class);

    @Test
    void supportsCustomAndStandardEnvironmentNames() {
        for (String secretName : new String[]{"AWS_SECRET_ACCESS", "AWS_SECRET_ACCESS_KEY"}) {
            context.withPropertyValues("AWS_ACCESS_KEY=test-access", secretName + "=test-secret").run(app -> {
                assertThat(app).hasNotFailed();
                var credentials = app.getBean(AwsCredentialsProvider.class).resolveCredentials();
                assertThat(credentials.accessKeyId()).isEqualTo("test-access");
                assertThat(credentials.secretAccessKey()).isEqualTo("test-secret");
                assertThat(app.getBean(S3Client.class).serviceClientConfiguration().region().id()).isEqualTo("us-east-1");
            });
        }
    }

    @Test
    void rejectsPartialConfiguration() {
        var config = new S3StorageConfig();
        assertThatThrownBy(() -> config.s3Credentials("test", "")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> config.s3Credentials("", "secret")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noExplicitCredentialsUsesTheDefaultChainWithoutResolvingItAtStartup() {
        try (var provider = (DefaultCredentialsProvider) new S3StorageConfig().s3Credentials("", "")) {
            assertThat(provider).isNotNull();
        }
    }
}
