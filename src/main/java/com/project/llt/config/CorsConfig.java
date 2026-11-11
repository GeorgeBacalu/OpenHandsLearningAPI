package com.project.llt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // This means all endpoints in your API.
                      .allowedOrigins("http://10.0.2.2:1434") // This is the Android emulator's alias to 'localhost' when using standard Android Studio emulator.
                      .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH") // Or other HTTP methods you want to allow.
                      .allowedHeaders("*") // Or specify headers if needed.
                      .allowCredentials(true);
            }
        };
    }
}
