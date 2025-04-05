package com.audition.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@Getter
public class SystemException extends RuntimeException {

    public static final String DEFAULT_TITLE = "API Error Occurred";
    public static final Integer DEFAULT_ERRORCODE = HttpStatus.INTERNAL_SERVER_ERROR.value();
    public static final String DEFAULT_DETAIL = "Please try again later or contact support.";
    private static final long serialVersionUID = -5876728854007114881L;
    private Integer statusCode;
    private String title;
    private String detail;

    public SystemException() {
        super();
    }

    public SystemException(final String message) {
        super(message);
        this.title = DEFAULT_TITLE;
        this.statusCode = DEFAULT_ERRORCODE;
        this.detail = DEFAULT_DETAIL;
    }

    public SystemException(final String message, final Integer errorCode) {
        super(message);
        this.title = DEFAULT_TITLE;
        this.statusCode = errorCode;
        this.detail = DEFAULT_DETAIL;
    }

    public SystemException(final String message, final Throwable exception) {
        super(message, exception);
        this.title = DEFAULT_TITLE;
        this.statusCode = getErrorcode(exception);
        this.detail = DEFAULT_DETAIL;
    }

    public SystemException(final String detail, final String title, final Integer errorCode) {
        super(detail);
        this.statusCode = errorCode;
        this.title = title;
        this.detail = detail;
    }

    public SystemException(final String detail, final String title, final Throwable exception) {
        super(detail, exception);
        this.title = title;
        this.statusCode = 500;
        this.detail = detail;
    }

    public SystemException(final String detail, final Integer errorCode, final Throwable exception) {
        super(detail, exception);
        this.statusCode = errorCode;
        this.title = DEFAULT_TITLE;
        this.detail = detail;
    }

    public SystemException(final String detail, final String title, final Integer errorCode,
        final Throwable exception) {
        super(detail, exception);
        this.statusCode = errorCode;
        this.title = title;
        this.detail = detail;
    }

    private Integer getErrorcode(final Throwable exception) {
        return (exception != null) ? ((HttpClientErrorException) exception).getStatusCode().value() : DEFAULT_ERRORCODE;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }
}
