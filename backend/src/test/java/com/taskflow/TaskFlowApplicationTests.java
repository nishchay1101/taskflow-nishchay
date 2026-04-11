package com.taskflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskFlowApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the full Spring application context boots successfully
        // against a real PostgreSQL instance provided by Testcontainers (test profile)
    }
}