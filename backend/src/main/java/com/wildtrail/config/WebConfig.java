package com.wildtrail.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig
implements WebMvcConfigurer {
    private final String[] origins;
    public WebConfig(@Value("${APP_ORIGINS:http://localhost:3000,http://127.0.0.1:3000}") String configured) {
        this.origins = configured.split(",");
    }
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origins).allowedMethods(new String[]{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"}).allowedHeaders(new String[]{"Content-Type", "Accept"}).allowCredentials(true).maxAge(3600L);
    }
}
