#!/bin/bash
# Run the application with OpenTelemetry Java Agent for local development

AGENT_PATH="docker/otel/opentelemetry-javaagent.jar"

if [ ! -f "$AGENT_PATH" ]; then
    echo "Agent not found. Downloading..."
    cd docker/otel && ./download-agent.sh && cd ../..
fi

export OTEL_SERVICE_NAME="simple-shop"
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4318"
export OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf"
export OTEL_LOGS_EXPORTER="otlp"
export OTEL_METRICS_EXPORTER="otlp"
export OTEL_TRACES_EXPORTER="otlp"

./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-javaagent:${AGENT_PATH}"
