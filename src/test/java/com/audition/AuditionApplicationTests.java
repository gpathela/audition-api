package com.audition;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuditionApplicationTests {

    // This test is used to check if the Spring application context loads successfully
    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {
        });
    }

}
