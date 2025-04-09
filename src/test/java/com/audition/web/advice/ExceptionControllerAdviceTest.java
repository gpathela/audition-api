package com.audition.web.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;

@SuppressWarnings("PMD.UnusedPrivateField")
@ExtendWith(MockitoExtension.class)
class ExceptionControllerAdviceTest {

    @InjectMocks
    private transient ExceptionControllerAdvice exceptionHandler;

    @Mock
    private transient AuditionLogger logger;

    @Test
    void handleHttpClientExceptionShouldReturnCorrectStatusAndMessage() {
        final HttpClientErrorException exception =
            new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid request");

        final ProblemDetail response = exceptionHandler.handleHttpClientException(exception);

        assertEquals(400, response.getStatus());
        assertEquals("API Error Occurred", response.getTitle());
        assertEquals("400 Invalid request", response.getDetail());
    }

    @Test
    void handleSystemExceptionShouldReturnStatusTitleAndMessage() {
        final SystemException exception = new SystemException("Custom Message", "Custom Title", 404);

        final ProblemDetail response = exceptionHandler.handleSystemException(exception);

        assertEquals(404, response.getStatus());
        assertEquals("Custom Title", response.getTitle());
        assertEquals("Custom Message", response.getDetail());
    }

    @Test
    void handleSystemExceptionWithInvalidStatusShouldReturn500() {
        final SystemException exception = new SystemException("Oops", "Invalid Code", 500);

        final ProblemDetail response = exceptionHandler.handleSystemException(exception);

        assertEquals(500, response.getStatus());
        assertEquals("Invalid Code", response.getTitle());
        assertEquals("Oops", response.getDetail());
    }

    @Test
    void handleMainExceptionShouldReturnInternalServerError() {
        final Exception exception = new RuntimeException("Something went wrong");

        final ProblemDetail response = exceptionHandler.handleMainException(exception);

        assertEquals(500, response.getStatus());
        assertEquals("API Error Occurred", response.getTitle());
        assertEquals("Something went wrong", response.getDetail());
    }

    @Test
    void createProblemDetailShouldFallbackToDefaultMessageIfBlank() {
        final Exception ex = new Exception((String) null);// message is null

        final ProblemDetail response = exceptionHandler.handleMainException(ex);

        assertEquals("API Error occurred. Please contact support or administrator.", response.getDetail());
        assertEquals("API Error Occurred", response.getTitle());
    }
}
