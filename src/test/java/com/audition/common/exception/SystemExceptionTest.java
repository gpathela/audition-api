package com.audition.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SystemExceptionTest {

    public static final String API_ERROR_OCCURRED = "API Error Occurred";

    @Test
    void defaultConstructorShouldCreateException() {
        final SystemException exception = new SystemException();
        assertNull(exception.getMessage());
        assertNull(exception.getTitle());
    }

    @Test
    void constructorWithMessageShouldSetTitleAndMessage() {
        final SystemException exception = new SystemException("Error occurred");
        assertEquals("Error occurred", exception.getMessage());
        assertEquals(API_ERROR_OCCURRED, exception.getTitle());
    }

    @Test
    void constructorWithMessageAndStatusCodeShouldSetAllFields() {
        final SystemException exception = new SystemException("Something failed", 400);
        assertEquals("Something failed", exception.getMessage());
        assertEquals(400, exception.getStatusCode());
        assertEquals(API_ERROR_OCCURRED, exception.getTitle());
    }

    @Test
    void constructorWithMessageAndThrowableShouldSetCauseAndTitle() {
        final Throwable cause = new RuntimeException("Root");
        final SystemException exception = new SystemException("Wrapped error", cause);

        assertEquals("Wrapped error", exception.getMessage());
        assertEquals(API_ERROR_OCCURRED, exception.getTitle());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructorWithDetailTitleAndStatusCodeShouldSetAllFields() {
        final SystemException exception = new SystemException("Detail message", "Custom Title", 403);

        assertEquals("Detail message", exception.getMessage());
        assertEquals("Custom Title", exception.getTitle());
        assertEquals("Detail message", exception.getDetail());
        assertEquals(403, exception.getStatusCode());
    }

    @Test
    void constructorWithDetailTitleAndThrowableShouldSetFieldsAndCause() {
        final Throwable cause = new IllegalArgumentException("Invalid");
        final SystemException exception = new SystemException("Detail", "Title", cause);

        assertEquals("Detail", exception.getMessage());
        assertEquals("Detail", exception.getDetail());
        assertEquals("Title", exception.getTitle());
        assertEquals(500, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructorWithDetailStatusCodeAndThrowableShouldSetFields() {
        final Throwable cause = new NullPointerException("Null");
        final SystemException exception = new SystemException("Message", 422, cause);

        assertEquals("Message", exception.getMessage());
        assertEquals("Message", exception.getDetail());
        assertEquals(422, exception.getStatusCode());
        assertEquals(API_ERROR_OCCURRED, exception.getTitle());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructorWithAllFieldsShouldSetEverythingCorrectly() {
        final Throwable cause = new Exception("Test");
        final SystemException exception = new SystemException("Detail msg", "My Title", 501, cause);

        assertEquals("Detail msg", exception.getMessage());
        assertEquals("Detail msg", exception.getDetail());
        assertEquals("My Title", exception.getTitle());
        assertEquals(501, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }
}
