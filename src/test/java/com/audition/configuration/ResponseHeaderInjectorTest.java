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
    void whenCurrentSpanExists_thenTraceHeadersAreSet() {
        // Arrange
        Tracer tracer = mock(Tracer.class);
        Span span = mock(Span.class);
        TraceContext context = mock(TraceContext.class);

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(context);
        when(context.traceId()).thenReturn("abc123");
        when(context.spanId()).thenReturn("xyz789");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        ResponseHeaderInjector interceptor = new ResponseHeaderInjector(tracer);

        // Act
        interceptor.afterCompletion(request, response, new Object(), null);

        // Assert
        verify(response).setHeader("X-Trace-Id", "abc123");
        verify(response).setHeader("X-Span-Id", "xyz789");
    }


    @Test
    void whenCurrentSpanIsNull_thenNoHeadersAreSet() {
        // Arrange
        Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenReturn(null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        ResponseHeaderInjector interceptor = new ResponseHeaderInjector(tracer);

        // Act
        interceptor.afterCompletion(request, response, new Object(), null);

        // Assert
        verify(response, never()).setHeader(eq("X-Trace-Id"), any());
        verify(response, never()).setHeader(eq("X-Span-Id"), any());
    }
}
