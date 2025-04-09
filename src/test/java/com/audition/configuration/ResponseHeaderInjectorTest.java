package com.audition.configuration;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

class ResponseHeaderInjectorTest {

    @Test
    void whenCurrentSpanExistsThenTraceHeadersAreSet() {
        // Arrange
        final Tracer tracer = mock(Tracer.class);
        final Span span = mock(Span.class);
        final TraceContext context = mock(TraceContext.class);

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(context);
        when(context.traceId()).thenReturn("abc123");
        when(context.spanId()).thenReturn("xyz789");

        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        final ResponseHeaderInjector interceptor = new ResponseHeaderInjector(tracer);

        // Act
        interceptor.afterCompletion(request, response, new Object(), null);

        // Assert
        verify(response).setHeader("X-Trace-Id", "abc123");
        verify(response).setHeader("X-Span-Id", "xyz789");
    }


    @Test
    void whenCurrentSpanIsNullThenNoHeadersAreSet() {
        // Arrange
        final Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenReturn(null);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        final ResponseHeaderInjector interceptor = new ResponseHeaderInjector(tracer);

        // Act
        interceptor.afterCompletion(request, response, new Object(), null);

        // Assert
        verify(response, never()).setHeader(eq("X-Trace-Id"), any());
        verify(response, never()).setHeader(eq("X-Span-Id"), any());
    }
}
