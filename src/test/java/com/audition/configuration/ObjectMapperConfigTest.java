package com.audition.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebServiceConfiguration.class)
@SuppressWarnings("PMD.UnusedPrivateField")
class ObjectMapperConfigTest {

    @Autowired
    private transient ObjectMapper objectMapper;
    @MockBean
    private transient ResponseHeaderInjector responseHeaderInjector;

    @Test
    void objectMapperShouldNotFailOnUnknownProperties() throws JsonProcessingException {
        final String json = """
                {
                  "userName": "test_user",
                  "extraField": "ignored"
                }
            """;

        final Sample result = objectMapper.readValue(json, Sample.class);
        assertEquals("test_user", result.userName);
    }

    @Test
    void objectMapperShouldWriteDateAsStringNotTimestamp() throws JsonProcessingException, ParseException {
        final Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse("2023-12-25"));
        final String output = objectMapper.writeValueAsString(map);
        assertTrue(output.contains("2023-12-25"));
    }

    @Test
    void objectMapperShouldExcludeNullValues() throws JsonProcessingException {
        final Sample sample = new Sample();
        sample.userName = "abc";

        final String json = objectMapper.writeValueAsString(sample);
        assertFalse(json.contains("unused"));
    }

    static class Sample {

        public String userName;

        public String getUserName() {
            return userName;
        }

        public void setUserName(final String userName) {
            this.userName = userName;
        }
    }
}
