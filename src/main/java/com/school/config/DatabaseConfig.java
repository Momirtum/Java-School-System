package com.school.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    public void configureDatabaseProperties(Environment env) {
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                URI dbUri = new URI(databaseUrl);
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

                System.setProperty("JDBC_DATABASE_URL", dbUrl);
                System.setProperty("JDBC_DATABASE_USERNAME", username);
                System.setProperty("JDBC_DATABASE_PASSWORD", password);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Error parsing DATABASE_URL", e);
            }
        }
    }
} 