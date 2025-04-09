package com.audition.common.logging;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

@ExtendWith(MockitoExtension.class)
class LoggingInterceptorTest {

    @Mock
    private transient HttpRequest httpRequest;

    @Mock
    private transient ClientHttpRequestExecution execution;

    @Mock
    private transient ClientHttpResponse mockResponse;

    private transient LoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new LoggingInterceptor();
    }

    @Test
    void shouldInterceptAndReturnResponse() throws IOException {
        final byte[] body = "test-body".getBytes(StandardCharsets.UTF_8);

        final URI uri = URI.create("http://localhost/test");
        when(httpRequest.getURI()).thenReturn(uri);
        when(httpRequest.getMethod()).thenReturn(HttpMethod.POST);
        when(execution.execute(httpRequest, body)).thenReturn(mockResponse);
        when(mockResponse.getBody()).thenReturn(
            new ByteArrayInputStream("response-body".getBytes(StandardCharsets.UTF_8)));
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        try (ClientHttpResponse result = interceptor.intercept(httpRequest, body, execution)) {
            assertNotNull(result);
            assertNotNull(result.getBody());
        }
    }
}