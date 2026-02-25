package com.example.ai_expanse_tacker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Centralized CORS configuration.
 * Allowed origins are controlled via the ALLOWED_ORIGINS environment variable.
 * Multiple origins can be separated by commas, e.g.:
 * ALLOWED_ORIGINS=https://yourapp.vercel.app,https://yourdomain.com
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");

        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}