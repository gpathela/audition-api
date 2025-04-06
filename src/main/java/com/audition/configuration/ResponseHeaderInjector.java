package com.audition.configuration;


import brave.internal.Nullable;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ResponseHeaderInjector implements HandlerInterceptor {

    private final Tracer tracer;

    public ResponseHeaderInjector(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler,
        @Nullable Exception ex) {

        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            response.setHeader("X-Trace-Id", currentSpan.context().traceId());
            response.setHeader("X-Span-Id", currentSpan.context().spanId());
        }
    }
}


