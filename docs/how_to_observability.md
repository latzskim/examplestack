# Observability Setup Guide for Spring Boot 4+

This guide explains how to set up complete observability (traces, metrics, logs) in a Spring Boot 4+ application using the OpenTelemetry Java Agent.

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Quick Start](#quick-start)
4. [Java Agent Setup](#java-agent-setup)
5. [Adding Custom Spans](#adding-custom-spans)
6. [Configuration Reference](#configuration-reference)
7. [Running the Observability Stack](#running-the-observability-stack)
8. [Viewing Traces in Grafana](#viewing-traces-in-grafana)
9. [Troubleshooting](#troubleshooting)

---

## Overview

The observability stack provides three pillars:

| Pillar | Purpose | Export Target |
|--------|---------|---------------|
| **Traces** | Distributed tracing across services and database calls | OTLP → Tempo |
| **Metrics** | JVM, HTTP, and custom metrics | OTLP → Mimir/Prometheus |
| **Logs** | Correlated logs with trace IDs | OTLP → Loki |

All telemetry is exported via **OTLP (OpenTelemetry Protocol)** to a Grafana LGTM stack.

### Why Java Agent?

The OpenTelemetry Java Agent provides:
- **Automatic instrumentation** for JDBC, HTTP clients, JMS, Hibernate, Spring, and 100+ libraries
- **Proper context propagation** ensuring correct parent-child span hierarchy
- **Zero code changes** for basic instrumentation
- **Consistent behavior** across all instrumented libraries

### What the Agent Provides

| Pillar | What's Automatic | Configuration |
|--------|------------------|---------------|
| **Traces** | HTTP requests, JDBC, Hibernate, Spring beans, async operations | Enabled by default |
| **Metrics** | JVM (memory, GC, threads), HTTP server metrics, connection pools | Enabled by default |
| **Logs** | Injects `traceId` and `spanId` into log MDC for correlation | Enabled by default |

All three pillars are exported via OTLP to your collector (Grafana LGTM stack).

---

## Prerequisites

- Java 21+
- Spring Boot 4.0+
- Docker & Docker Compose (for local observability backend)

---

## Quick Start

```bash
# 1. Start the observability stack
cd docker && docker-compose up -d

# 2. Download the OpenTelemetry Java Agent
cd otel && ./download-agent.sh && cd ../..

# 3. Run the application with the agent
./run-with-agent.sh

# 4. Open Grafana to view traces
open http://localhost:3000
```

---

## Java Agent Setup

### Step 1: Download the Agent

```bash
cd docker/otel
./download-agent.sh
```

Or download manually:

```bash
curl -sL https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.15.0/opentelemetry-javaagent.jar -o opentelemetry-javaagent.jar
```

### Step 2: Add Annotations Dependency

Add to `pom.xml` for custom span support:

```xml
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-instrumentation-annotations</artifactId>
</dependency>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.opentelemetry.instrumentation</groupId>
            <artifactId>opentelemetry-instrumentation-bom</artifactId>
            <version>2.15.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Step 3: Run with the Agent

**Option A: Use the helper script**

```bash
./run-with-agent.sh
```

**Option B: Run manually**

```bash
export OTEL_SERVICE_NAME="simple-shop"
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4318"
export OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf"
export OTEL_LOGS_EXPORTER="otlp"
export OTEL_METRICS_EXPORTER="otlp"
export OTEL_TRACES_EXPORTER="otlp"

./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-javaagent:docker/otel/opentelemetry-javaagent.jar"
```

**Option C: Run in Docker**

The Dockerfile automatically includes the agent:

```dockerfile
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# OpenTelemetry Java Agent
ARG OTEL_AGENT_VERSION=2.15.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"
ENV OTEL_SERVICE_NAME="simple-shop"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://otel-lgtm:4318"
ENV OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf"
ENV OTEL_LOGS_EXPORTER="otlp"
ENV OTEL_METRICS_EXPORTER="otlp"
ENV OTEL_TRACES_EXPORTER="otlp"

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Adding Custom Spans

The agent automatically creates spans for HTTP requests, JDBC queries, and many frameworks. For business-level tracing, add custom spans using `@WithSpan`.

### Using @WithSpan Annotation

```java
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;

@Service
public class OrderService {

    @WithSpan("order.placeOrder")
    public OrderView placeOrder(PlaceOrderCommand command) {
        // Method execution is wrapped in a span
        return doPlaceOrder(command);
    }

    @WithSpan("order.getOrder")
    public Optional<OrderView> getOrder(@SpanAttribute("orderId") UUID orderId) {
        // orderId will be added as a span attribute
        return orderRepository.findById(orderId);
    }
}
```

### Naming Convention

Use `module.operation` format for span names:
- `order.placeOrder`
- `catalog.getProduct`
- `inventory.reserveStock`
- `notification.sendEmail`

### Using @SpanAttribute for Context

Add important identifiers as span attributes:

```java
@WithSpan("shipping.trackShipment")
public ShipmentView track(@SpanAttribute("trackingNumber") String trackingNumber) {
    // trackingNumber will appear as an attribute in the span
}
```

### Programmatic Span Creation

For more control, use the OpenTelemetry API directly:

```java
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@Service
public class PaymentService {

    private final Tracer tracer;

    public PaymentService(Tracer tracer) {
        this.tracer = tracer;
    }

    public PaymentResult processPayment(PaymentRequest request) {
        Span span = tracer.spanBuilder("payment.process").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("payment.amount", request.getAmount().toString());
            span.setAttribute("payment.currency", request.getCurrency());
            
            // Process payment...
            
            return result;
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

---

## Configuration Reference

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OTEL_SERVICE_NAME` | Service name in traces | `unknown_service:java` |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OTLP collector endpoint | `http://localhost:4318` |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | Protocol (`http/protobuf` or `grpc`) | `http/protobuf` |
| `OTEL_TRACES_EXPORTER` | Traces exporter (`otlp`, `none`) | `otlp` |
| `OTEL_METRICS_EXPORTER` | Metrics exporter (`otlp`, `none`) | `otlp` |
| `OTEL_LOGS_EXPORTER` | Logs exporter (`otlp`, `none`) | `otlp` |
| `OTEL_TRACES_SAMPLER` | Sampling strategy | `parentbased_always_on` |
| `OTEL_TRACES_SAMPLER_ARG` | Sampler argument (e.g., ratio) | `1.0` |

### Instrumentation Controls

| Variable | Description | Default |
|----------|-------------|---------|
| `OTEL_INSTRUMENTATION_JDBC_ENABLED` | Enable JDBC tracing | `true` |
| `OTEL_INSTRUMENTATION_HIBERNATE_ENABLED` | Enable Hibernate tracing | `true` |
| `OTEL_INSTRUMENTATION_SPRING_WEB_ENABLED` | Enable Spring Web tracing | `true` |
| `OTEL_INSTRUMENTATION_LOGBACK_APPENDER_ENABLED` | Export logs via OTLP | `true` |
| `OTEL_INSTRUMENTATION_LOGBACK_MDC_ENABLED` | Inject traceId/spanId into MDC | `true` |

### Metrics Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `OTEL_METRICS_EXPORTER` | Metrics exporter (`otlp`, `none`) | `otlp` |
| `OTEL_METRIC_EXPORT_INTERVAL` | Export interval in milliseconds | `60000` |

**Metrics automatically collected:**
- JVM: memory, GC, threads, class loading
- HTTP server: request count, duration, response sizes
- Connection pools: active connections, idle, pending

### Logs Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `OTEL_LOGS_EXPORTER` | Logs exporter (`otlp`, `none`) | `otlp` |

**How log correlation works:**
1. Agent injects `traceId` and `spanId` into Logback/Log4j MDC
2. Your log pattern can include these: `%X{traceId}` and `%X{spanId}`
3. Logs are also exported via OTLP to Loki (if enabled)
4. In Grafana, you can jump from a trace to its correlated logs

### Production Settings

For production, consider:

```bash
# Sample only 10% of traces
export OTEL_TRACES_SAMPLER="parentbased_traceidratio"
export OTEL_TRACES_SAMPLER_ARG="0.1"

# Disable metrics if not needed
export OTEL_METRICS_EXPORTER="none"
```

---

## Running the Observability Stack

### Docker Compose

The `docker/docker-compose.yml` includes the Grafana LGTM stack:

```yaml
services:
  # OpenTelemetry LGTM Stack (Loki + Grafana + Tempo + Mimir)
  otel-lgtm:
    image: grafana/otel-lgtm:latest
    ports:
      - "3000:3000"    # Grafana UI
      - "4317:4317"    # OTLP gRPC
      - "4318:4318"    # OTLP HTTP
    environment:
      - OTEL_LOG_LEVEL=info
```

### Start the Stack

```bash
cd docker
docker-compose up -d
```

### Verify Endpoints

| Endpoint | Purpose |
|----------|---------|
| http://localhost:3000 | Grafana UI (admin/admin) |
| http://localhost:4318 | OTLP HTTP receiver |
| http://localhost:4317 | OTLP gRPC receiver |

---

## Viewing Traces in Grafana

1. Open http://localhost:3000
2. Login (default: admin/admin)
3. Go to **Explore** (compass icon)
4. Select **Tempo** as the data source
5. Use **Search** to find traces by:
   - Service name
   - Span name
   - Duration
   - Attributes (e.g., `orderId`, `trackingNumber`)

### Trace View Features

- **Waterfall diagram**: Shows span hierarchy and timing
- **Span details**: Click on a span to see attributes
- **Logs correlation**: Jump to related logs in Loki
- **Service map**: Visualize service dependencies

### Example Trace Hierarchy

```
GET /orders/{id}                           (50ms)
├── OrderController.getOrder               (45ms)
│   └── order.getOrder                     (40ms)  ← @WithSpan
│       ├── SELECT orders                  (5ms)   ← JDBC auto-instrumented
│       └── SELECT order_items             (3ms)   ← JDBC auto-instrumented
└── TraceIdFilter                          (1ms)
```

---

## Troubleshooting

### No Traces Appearing

1. **Check OTLP endpoint is reachable**:
   ```bash
   curl -v http://localhost:4318/v1/traces
   ```

2. **Check agent is loaded** - look for this in logs:
   ```
   [otel.javaagent] opentelemetry-javaagent - version: 2.15.0
   ```

3. **Check environment variables**:
   ```bash
   echo $OTEL_SERVICE_NAME
   echo $OTEL_EXPORTER_OTLP_ENDPOINT
   ```

### Connection Refused to OTLP Endpoint

Start the observability stack:
```bash
cd docker && docker-compose up -d
docker-compose ps  # Verify otel-lgtm is running
```

### Spans Not Nested Correctly

If spans appear as siblings instead of parent-child:

1. **Verify you're using the Java Agent** (not manual SDK)
2. **Check `@WithSpan` is on the right method** - it must be called through Spring proxy
3. **For async operations**, the Java Agent automatically propagates context across threads

### Missing @WithSpan Spans

The annotation requires:
1. Method must be `public`
2. Must be called through Spring proxy (not `this.method()`)
3. Class must be a Spring bean (`@Service`, `@Component`, etc.)

### High Memory Usage

The agent adds ~50-100MB overhead. To reduce:
```bash
# Disable unused instrumentation
export OTEL_INSTRUMENTATION_KAFKA_ENABLED=false
export OTEL_INSTRUMENTATION_GRPC_ENABLED=false

# Reduce metric collection
export OTEL_METRICS_EXPORTER=none
```

---

## Optional: TraceIdFilter

To include trace IDs in HTTP response headers:

```java
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
```

Clients can use this header to correlate requests with traces in Grafana.

---

## Summary

| Component | Purpose |
|-----------|---------|
| `opentelemetry-javaagent.jar` | Auto-instruments JDBC, HTTP, Spring, etc. |
| `opentelemetry-instrumentation-annotations` | `@WithSpan` and `@SpanAttribute` |
| `run-with-agent.sh` | Helper script for local development |
| `Dockerfile` | Includes agent for containerized deployment |
| `TraceIdFilter` | Adds trace ID to HTTP response headers |
| `grafana/otel-lgtm` | All-in-one observability backend |
