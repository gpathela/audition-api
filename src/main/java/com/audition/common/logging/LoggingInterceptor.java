package com.audition.common.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(
        final HttpRequest request,
        final byte[] body,
        final ClientHttpRequestExecution execution
    ) throws IOException {

        logRequest(request, body);
        final ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);

        return response;
    }

    private void logRequest(final HttpRequest request, final byte[] body) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Request URI: {}", request.getURI());
            LOGGER.info("Request Method: {}", request.getMethod());
            LOGGER.info("➡️ Request Body: {}", new String(body, StandardCharsets.UTF_8));
        }
    }

    private void logResponse(final ClientHttpResponse response) throws IOException {
        if (LOGGER.isInfoEnabled()) {
            final String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
            LOGGER.info("⬅️ Response Status: {}", response.getStatusCode());
            LOGGER.info("⬅️ Response Body: {}", responseBody);
        }
    }
}

