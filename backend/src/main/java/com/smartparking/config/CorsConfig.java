package com.smartparking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CorsConfig - Cross-Origin Resource Sharing (CORS) Configuration.
 *
 * CORS is a browser security feature that blocks requests from one origin
 * to another by default.
 *
 * Our situation:
 * - React frontend runs at: http://localhost:5173 (Vite dev server)
 * - Spring Boot backend runs at: http://localhost:8080
 *
 * Without this configuration, the browser would block the React app from
 * calling our backend APIs (even though they're on the same computer).
 *
 * This configuration tells Spring Boot:
 * "Allow HTTP requests from http://localhost:5173 to reach our APIs"
 *
 * @Configuration - This class provides Spring configuration (Spring reads it at startup)
 * @Bean - Methods annotated with @Bean register objects into the Spring container
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // Configuration object that specifies CORS rules
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Allow requests from the React development server
        corsConfiguration.addAllowedOrigin("http://localhost:5173");

        // Also allow from localhost:3000 (in case user runs React differently)
        corsConfiguration.addAllowedOrigin("http://localhost:3000");

        // Allow all HTTP headers (Content-Type, Authorization, etc.)
        corsConfiguration.addAllowedHeader("*");

        // Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
        corsConfiguration.addAllowedMethod("*");

        // Allow cookies and credentials to be sent with requests
        corsConfiguration.setAllowCredentials(true);

        // Apply this CORS configuration to ALL API endpoints ("/**" means all paths)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}
