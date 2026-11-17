package com.project.ohl.adapter.storage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.StandardEnvironment;
import com.project.ohl.application.service.MediaServiceImpl;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

class S3StorageConfigTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withInitializer(app -> {
                app.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                new ConfigDataApplicationContextInitializer().initialize(app);
            })
            .withPropertyValues("spring.config.location=file:src/main/resources/application.properties",
                    "AWS_ACCESS_KEY=test-access", "AWS_SECRET_ACCESS=test-secret")
            .withBean(PropertySourcesPlaceholderConfigurer.class)
            .withUserConfiguration(S3StorageConfig.class, S3MediaStorageAdapter.class);

    @Test
    void readsExplicitEnvironmentCredentials() {
        context.run(app -> {
            assertThat(app).hasNotFailed();
            var credentials = app.getBean(AwsCredentialsProvider.class).resolveCredentials();
            assertThat(credentials.accessKeyId()).isEqualTo("test-access");
            assertThat(credentials.secretAccessKey()).isEqualTo("test-secret");
        });
    }

    @Test
    void clientAndStorageUseRegionAndBucketFromApplicationProperties() {
        context.run(app -> {
            assertThat(app).hasNotFailed();

            var environment = app.getEnvironment();

            String region = environment.getRequiredProperty("app.storage.s3.region");
            String bucket = environment.getRequiredProperty("app.storage.s3.bucket");

            assertThat(app.getBean(S3Client.class).serviceClientConfiguration().region().id()).isEqualTo(region);
            assertThat(app.getBean(S3MediaStorageAdapter.class).download("dev/categories/main.webp").url())
                    .isEqualTo("https://" + bucket + ".s3." + region + ".amazonaws.com/dev/categories/main.webp");
        });
    }

    @Test
    void testProfileUsesH2WhileInheritingStorageConfiguration() {
        new ApplicationContextRunner()
                .withInitializer(app -> {
                    app.getEnvironment().setActiveProfiles("test");
                    new ConfigDataApplicationContextInitializer().initialize(app);
                })
                .withBean(PropertySourcesPlaceholderConfigurer.class)
                .withUserConfiguration(S3StorageConfig.class, S3MediaStorageAdapter.class)
                .run(app -> {
                    assertThat(app).hasNotFailed();
                    var environment = app.getEnvironment();

                    assertThat(environment.getRequiredProperty("spring.datasource.url"))
                            .isEqualTo("jdbc:h2:mem:open-hands-learning-test");
                    assertThat(environment.getRequiredProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("create-drop");
                    assertThat(app.getBean(S3Client.class).serviceClientConfiguration().region().id())
                            .isEqualTo(environment.getRequiredProperty("app.storage.s3.region"));
                    assertThat(app.getBean(S3MediaStorageAdapter.class).download("dev/categories/main.webp").url())
                            .contains("https://" + environment.getRequiredProperty("app.storage.s3.bucket") + ".s3.");
                });
    }

    @Test
    void awsConfigurationRejectsPartialCredentials() {
        for (String missing : new String[]{"AWS_ACCESS_KEY", "AWS_SECRET_ACCESS"}) {
            context.withPropertyValues(missing + "=").run(app -> assertThat(app).hasFailed());
        }
    }

    @Test
    void emptyCredentialsUseTheDefaultChainWithoutResolvingItAtStartup() {
        context.withPropertyValues("AWS_ACCESS_KEY=", "AWS_SECRET_ACCESS=").run(app -> {
            assertThat(app).hasNotFailed();
            assertThat(app.getBean(AwsCredentialsProvider.class)).isInstanceOf(DefaultCredentialsProvider.class);
            assertThat(app.getBean(S3Client.class)).isNotNull();
        });
    }

    private ApplicationContextRunner production(String profile) {
        return new ApplicationContextRunner()
                .withInitializer(app -> {
                    app.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                    app.getEnvironment().setActiveProfiles(profile);
                    new ConfigDataApplicationContextInitializer().initialize(app);
                })
                .withPropertyValues("spring.config.location=file:src/main/resources/application.properties",
                        "DATABASE_JDBC_URL=jdbc:postgresql://neon.example/app",
                        "NEON_STORAGE_ACCESS_KEY_ID=neon-access", "NEON_STORAGE_SECRET_ACCESS_KEY=neon-secret",
                        "AWS_ACCESS_KEY=dev-access", "AWS_SECRET_ACCESS=dev-secret")
                .withBean(PropertySourcesPlaceholderConfigurer.class)
                .withUserConfiguration(S3StorageConfig.class, S3MediaStorageAdapter.class, MediaServiceImpl.class);
    }

    @Test
    void productionAndRenderUseNeonForDatabaseCredentialsClientAndPublicUrls() {
        for (String profile : new String[]{"prod", "render"}) {
            production(profile).run(app -> {
                assertThat(app).hasNotFailed();
                var environment = app.getEnvironment();
                assertThat(environment.getActiveProfiles()).contains("prod");
                assertThat(environment.getRequiredProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://neon.example/app");
                assertThat(environment.getRequiredProperty("spring.flyway.enabled")).isEqualTo("true");
                var credentials = app.getBean(AwsCredentialsProvider.class).resolveCredentials();
                assertThat(credentials.accessKeyId()).isEqualTo("neon-access");
                assertThat(credentials.secretAccessKey()).isEqualTo("neon-secret");
                var client = app.getBean(S3Client.class);
                String expected = "https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/prod/categories/alfabet/a/main.webp";
                assertThat(client.serviceClientConfiguration().region().id()).isEqualTo("eu-central-1");
                // Resolving this URL exercises the actual SDK endpoint + path-style configuration without network access.
                assertThat(client.utilities().getUrl(request -> request.bucket("openhandslearning-media")
                        .key("prod/categories/alfabet/a/main.webp")).toExternalForm()).isEqualTo(expected);
                assertThat(app.getBean(MediaServiceImpl.class).download("images", "categories/alfabet/a/main.webp").url())
                        .isEqualTo(expected);
            });
        }
    }

    @Test
    void productionNeverFallsBackToAwsCredentialsWhenNeonCredentialsAreMissing() {
        for (String missing : new String[]{"NEON_STORAGE_ACCESS_KEY_ID", "NEON_STORAGE_SECRET_ACCESS_KEY"}) {
            production("prod").withPropertyValues(missing + "=").run(app -> assertThat(app).hasFailed());
        }
        production("render").withPropertyValues("NEON_STORAGE_ACCESS_KEY_ID=", "NEON_STORAGE_SECRET_ACCESS_KEY=")
                .run(app -> assertThat(app).hasFailed());
    }

    @Test
    void customEndpointRejectsInsecureUrlsAndEmbeddedCredentials() {
        for (String endpoint : new String[]{"http://storage.example", "https://user:password@storage.example", "https://storage.example/?token=value"}) {
            production("prod").withPropertyValues("app.storage.s3.endpoint=" + endpoint)
                    .run(app -> assertThat(app).hasFailed());
        }
    }
}
