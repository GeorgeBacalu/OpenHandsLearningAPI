package com.project.ohl.adapter.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfig {

    private final String timeZone;

    public ProjectConfig(@Value("${app.timezone}") String timeZone) {
        this.timeZone = timeZone;
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(timeZone));
    }
}
