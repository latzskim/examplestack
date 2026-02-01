package com.simpleshop.shared.observability;

import io.opentelemetry.api.trace.Span;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Span currentSpan = Span.current();
        if (currentSpan.getSpanContext().isValid()) {
            response.setHeader("X-Trace-Id", currentSpan.getSpanContext().getTraceId());
        }
        filterChain.doFilter(request, response);
    }
}
