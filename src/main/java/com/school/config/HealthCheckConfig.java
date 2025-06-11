package com.school.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StudentsHealthIndicator implements HealthIndicator {
    
    private final RestTemplate restTemplate;
    
    public StudentsHealthIndicator() {
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public Health health() {
        try {
            String url = "http://localhost:" + System.getenv().getOrDefault("PORT", "8080") + "/api/students";
            restTemplate.getForObject(url, Object.class);
            return Health.up().build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
} 