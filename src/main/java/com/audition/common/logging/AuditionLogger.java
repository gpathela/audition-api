package com.audition.common.logging;

import org.slf4j.Logger;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class AuditionLogger {

    private static final String DEFAULT_BASIC_RESPONSE_MESSAGE = "Error Code: %s - %s";
    private static final String DEFAULT_PROBLEM_DETAIL_MESSAGE = "Title: %s\nStatus: %s\nDetail: %s\nInstance: %s";

    public void info(final Logger logger, final String message) {
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
    }

    public void info(final Logger logger, final String message, final Object object) {
        if (logger.isInfoEnabled()) {
            logger.info(message, object);
        }
    }

    public void debug(final Logger logger, final String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    public void warn(final Logger logger, final String message) {
        if (logger.isWarnEnabled()) {
            logger.warn(message);
        }
    }

    public void error(final Logger logger, final String message) {
        if (logger.isErrorEnabled()) {
            logger.error(message);
        }
    }

    public void logErrorWithException(final Logger logger, final String message, final Exception e) {
        if (logger.isErrorEnabled()) {
            logger.error(message, e);
        }
    }

    public void logStandardProblemDetail(final Logger logger, final ProblemDetail problemDetail, final Exception e) {
        if (logger.isErrorEnabled()) {
            final var message = createStandardProblemDetailMessage(problemDetail);
            logger.error(message, e);
        }
    }

    public void logHttpStatusCodeError(final Logger logger, final String message, final Integer errorCode) {
        if (logger.isErrorEnabled()) {
            logger.error(createBasicErrorResponseMessage(errorCode, message) + "\n");
        }
    }

    private String createStandardProblemDetailMessage(final ProblemDetail standardProblemDetail) {
        return String.format(DEFAULT_PROBLEM_DETAIL_MESSAGE,
            standardProblemDetail.getTitle(),
            standardProblemDetail.getStatus(),
            standardProblemDetail.getDetail(),
            standardProblemDetail.getInstance());
    }

    private String createBasicErrorResponseMessage(final Integer errorCode, final String message) {
        return String.format(DEFAULT_BASIC_RESPONSE_MESSAGE, errorCode, message);
    }
}
