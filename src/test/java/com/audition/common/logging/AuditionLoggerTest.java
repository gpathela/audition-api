package com.audition.common.logging;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.ProblemDetail;

@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
@ExtendWith(MockitoExtension.class)
class AuditionLoggerTest {

    @Mock
    private transient Logger mockLogger;

    @InjectMocks
    private transient AuditionLogger auditionLogger;

    @Nested
    class WarnTests {

        @Test
        void shouldLogWarnMessageWhenWarnIsEnabled() {
            when(mockLogger.isWarnEnabled()).thenReturn(true);

            auditionLogger.warn(mockLogger, "Warn message");

            verify(mockLogger).warn("Warn message");
        }

        @Test
        void shouldNotLogWarnMessageWhenWarnIsDisabled() {
            when(mockLogger.isWarnEnabled()).thenReturn(false);

            auditionLogger.warn(mockLogger, "Warn message");

            verify(mockLogger, never()).warn(anyString());
        }
    }


    @Nested
    class DebugTests {

        @Test
        void shouldLogDebugMessageWhenDebugIsEnabled() {
            when(mockLogger.isDebugEnabled()).thenReturn(true);

            auditionLogger.debug(mockLogger, "Debug message");

            verify(mockLogger).debug("Debug message");
        }

        @Test
        void shouldNotLogDebugMessageWhenDebugIsDisabled() {
            when(mockLogger.isDebugEnabled()).thenReturn(false);

            auditionLogger.debug(mockLogger, "Debug message");

            verify(mockLogger, never()).debug(anyString());
        }

    }

    @Nested
    class ErrorTests {

        @Test
        void shouldLogErrorMessageWhenErrorIsEnabled() {
            when(mockLogger.isErrorEnabled()).thenReturn(true);

            auditionLogger.error(mockLogger, "Error message");

            verify(mockLogger).error("Error message");
        }

        @Test
        void shouldNotLogErrorMessageWhenErrorIsDisabled() {
            when(mockLogger.isErrorEnabled()).thenReturn(false);

            auditionLogger.error(mockLogger, "Error message");

            verify(mockLogger, never()).error(anyString());
        }

        @Test
        void shouldLogErrorWithExceptionWhenErrorIsEnabled() {
            when(mockLogger.isErrorEnabled()).thenReturn(true);
            final Exception e = new RuntimeException("Test exception");

            auditionLogger.logErrorWithException(mockLogger, "Exception occurred", e);

            verify(mockLogger).error("Exception occurred", e);
        }

        @Test
        void shouldLogStandardProblemDetailWhenErrorIsEnabled() {
            when(mockLogger.isErrorEnabled()).thenReturn(true);

            final ProblemDetail detail = ProblemDetail.forStatus(500);
            detail.setTitle("Internal Error");
            detail.setDetail("Something went wrong");
            detail.setInstance(URI.create("/test"));

            final Exception e = new RuntimeException("Problem detail error");

            auditionLogger.logStandardProblemDetail(mockLogger, detail, e);

            verify(mockLogger).error(contains("Title: Internal Error"), eq(e));
        }

        @Test
        void shouldLogHttpStatusCodeErrorWhenErrorIsEnabled() {
            when(mockLogger.isErrorEnabled()).thenReturn(true);

            auditionLogger.logHttpStatusCodeError(mockLogger, "Bad Request", 400);

            verify(mockLogger).error(contains("Error Code: 400 - Bad Request"));
        }

        @Test
        void shouldNotLogHttpStatusCodeErrorWhenErrorIsDisabled() {
            when(mockLogger.isErrorEnabled()).thenReturn(false);

            auditionLogger.logHttpStatusCodeError(mockLogger, "Bad Request", 400);

            verify(mockLogger, never()).error(anyString());
        }

    }


    @Nested
    class InfoTests {

        @Test
        void shouldLogInfoMessageWhenInfoIsEnabled() {
            when(mockLogger.isInfoEnabled()).thenReturn(true);

            auditionLogger.info(mockLogger, "Test message");

            verify(mockLogger).info("Test message");
        }

        @Test
        void shouldNotLogInfoMessageWhenInfoIsDisabled() {
            when(mockLogger.isInfoEnabled()).thenReturn(false);

            auditionLogger.info(mockLogger, "Test message");

            verify(mockLogger, never()).info(anyString());
        }

        @Test
        void shouldLogInfoMessageWithObjectWhenInfoIsEnabled() {
            when(mockLogger.isInfoEnabled()).thenReturn(true);

            auditionLogger.info(mockLogger, "Value is {}", 123);

            verify(mockLogger).info("Value is {}", 123);
        }

    }
}
