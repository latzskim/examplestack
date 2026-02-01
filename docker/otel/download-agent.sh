#!/bin/bash
# Downloads the OpenTelemetry Java Agent

OTEL_VERSION="2.15.0"
AGENT_JAR="opentelemetry-javaagent.jar"

if [ -f "$AGENT_JAR" ]; then
    echo "Agent already exists: $AGENT_JAR"
    exit 0
fi

echo "Downloading OpenTelemetry Java Agent v${OTEL_VERSION}..."
curl -sL "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar" -o "$AGENT_JAR"

if [ -f "$AGENT_JAR" ]; then
    echo "Downloaded successfully: $AGENT_JAR"
else
    echo "Failed to download agent"
    exit 1
fi
