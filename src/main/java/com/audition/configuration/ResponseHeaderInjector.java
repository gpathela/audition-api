package com.audition.configuration;


import brave.internal.Nullable;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ResponseHeaderInjector implements HandlerInterceptor, Serializable {

    private static final long serialVersionUID = 1L;
    private final transient Tracer tracer;

    public ResponseHeaderInjector(final Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void afterCompletion(@NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final Object handler,
        @Nullable final Exception ex) {

        final Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            response.setHeader("X-Trace-Id", currentSpan.context().traceId());
            response.setHeader("X-Span-Id", currentSpan.context().spanId());
        }
    }
}


