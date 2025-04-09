package com.audition.configuration;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WebServiceConfiguration.class, ResponseHeaderInjector.class})
class RestTemplateConfigTest {

    @Autowired
    private transient RestTemplate restTemplate;

    @Autowired
    private transient ObjectMapper objectMapper;

    @Test
    void restTemplateShouldHaveLoggingInterceptor() {
        final boolean hasLogging = restTemplate.getInterceptors().stream()
            .anyMatch(i -> "LoggingInterceptor".equals(i.getClass().getSimpleName()));
        assertTrue(hasLogging);
    }

    @Test
    void restTemplateShouldUseConfiguredObjectMapper() {
        final MappingJackson2HttpMessageConverter converter = restTemplate.getMessageConverters().stream()
            .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
            .map(c -> (MappingJackson2HttpMessageConverter) c)
            .findFirst()
            .orElseThrow();

        assertSame(objectMapper, converter.getObjectMapper());
    }

    @TestConfiguration
    static class MockBeans {

        @Bean
        public ResponseHeaderInjector responseHeaderInjector() {
            return Mockito.mock(ResponseHeaderInjector.class);
        }
    }
}
