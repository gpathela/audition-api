package com.audition.configuration;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebServiceConfiguration.class)
class RestTemplateConfigTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void restTemplate_shouldHaveLoggingInterceptor() {
        boolean hasLogging = restTemplate.getInterceptors().stream()
            .anyMatch(i -> i.getClass().getSimpleName().equals("LoggingInterceptor"));
        assertTrue(hasLogging);
    }

    @Test
    void restTemplate_shouldUseConfiguredObjectMapper() {
        MappingJackson2HttpMessageConverter converter = restTemplate.getMessageConverters().stream()
            .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
            .map(c -> (MappingJackson2HttpMessageConverter) c)
            .findFirst()
            .orElseThrow();

        assertSame(objectMapper, converter.getObjectMapper());
    }
}
