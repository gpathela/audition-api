package com.audition.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebServiceConfiguration.class)
class ObjectMapperConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void objectMapper_shouldNotFailOnUnknownProperties() throws Exception {
        String json = """
                {
                  "userName": "test_user",
                  "extraField": "ignored"
                }
            """;

        Sample result = objectMapper.readValue(json, Sample.class);
        assertEquals("test_user", result.userName);
    }

    @Test
    void objectMapper_shouldWriteDateAsString_notTimestamp() throws Exception {
        Map<String, Object> map = Map.of("date", new SimpleDateFormat("yyyy-MM-dd").parse("2023-12-25"));
        String output = objectMapper.writeValueAsString(map);
        assertTrue(output.contains("2023-12-25"));
    }

    @Test
    void objectMapper_shouldExcludeNullValues() throws Exception {
        Sample sample = new Sample();
        sample.userName = "abc";
        sample.unused = null;

        String json = objectMapper.writeValueAsString(sample);
        assertFalse(json.contains("unused"));
    }

    static class Sample {

        public String userName;
        public String unused;
    }
}
