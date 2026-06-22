package com.iarecruiter.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("ia_recruiter_test")
            .withUsername("ia_recruiter")
            .withPassword("testpassword");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // disable rabbitmq for this test
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.autoconfigure.exclude", () ->
                "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void register_and_login_flow() {
        // POST /api/auth/register
        var registerBody = Map.of(
                "companyName", "Test Corp",
                "email", "test@testcorp.com",
                "password", "SecurePass123!",
                "firstName", "John",
                "lastName", "Doe"
        );
        var registerResp = restTemplate.postForEntity("/api/auth/register", registerBody, Map.class);
        assertThat(registerResp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(registerResp.getBody()).containsKey("accessToken");

        // POST /api/auth/login
        var loginBody = Map.of("email", "test@testcorp.com", "password", "SecurePass123!");
        var loginResp = restTemplate.postForEntity("/api/auth/login", loginBody, Map.class);
        assertThat(loginResp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(loginResp.getBody()).containsKey("accessToken");
    }
}
