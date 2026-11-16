package com.project.ohl.adapter.storage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.StandardEnvironment;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3StorageConfigTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withInitializer(app -> {
                app.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                new ConfigDataApplicationContextInitializer().initialize(app);
            })
            .withPropertyValues("spring.config.location=file:src/main/resources/application.properties")
            .withBean(PropertySourcesPlaceholderConfigurer.class)
            .withUserConfiguration(S3StorageConfig.class);

    @Test
    void supportsCustomAndStandardEnvironmentNames() {
        for (String[] names : new String[][]{{"AWS_ACCESS_KEY", "AWS_SECRET_ACCESS"},
                {"AWS_ACCESS_KEY_ID", "AWS_SECRET_ACCESS_KEY"}}) {
            context.withPropertyValues(names[0] + "=test-access", names[1] + "=test-secret").run(app -> {
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
    void explicitSpringCredentialsOverrideEnvironmentCredentials() {
        context.withPropertyValues("app.storage.s3.access-key=explicit-access", "app.storage.s3.secret-key=explicit-secret",
                "AWS_ACCESS_KEY=environment-access", "AWS_SECRET_ACCESS=environment-secret").run(app -> {
            assertThat(app).hasNotFailed();
            var credentials = app.getBean(AwsCredentialsProvider.class).resolveCredentials();
            assertThat(credentials.accessKeyId()).isEqualTo("explicit-access");
            assertThat(credentials.secretAccessKey()).isEqualTo("explicit-secret");
        });
    }

    @Test
    void productionConfigurationRejectsPartialCredentials() {
        context.withPropertyValues("AWS_ACCESS_KEY=test-access").run(app -> {
            assertThat(app).hasFailed();
            assertThat(app.getStartupFailure()).hasRootCauseInstanceOf(IllegalArgumentException.class);
        });
    }

    @Test
    void noExplicitCredentialsUsesTheDefaultChainWithoutResolvingItAtStartup() {
        context.run(app -> {
            assertThat(app).hasNotFailed();
            assertThat(app.getBean(AwsCredentialsProvider.class)).isInstanceOf(DefaultCredentialsProvider.class);
            assertThat(app.getBean(S3Client.class)).isNotNull();
        });
    }
}
