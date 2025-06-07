#!/bin/bash

# Set OpenTelemetry environment variables
export OTEL_SERVICE_NAME=payment-service
export OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://localhost:4318/v1/traces
export OTEL_TRACES_EXPORTER=otlp
export OTEL_METRICS_EXPORTER=none
export OTEL_LOGS_EXPORTER=none
export OTEL_INSTRUMENTATION_JAXRS_ENABLED=true
export OTEL_INSTRUMENTATION_CDI_ENABLED=true
export OTEL_TRACES_SAMPLER=always_on
export OTEL_RESOURCE_ATTRIBUTES=service.name=payment-service,service.version=1.0.0

echo "🔧 OpenTelemetry environment variables set:"
echo "   OTEL_SERVICE_NAME=$OTEL_SERVICE_NAME"
echo "   OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=$OTEL_EXPORTER_OTLP_TRACES_ENDPOINT"
echo "   OTEL_TRACES_EXPORTER=$OTEL_TRACES_EXPORTER"

# Start Liberty with telemetry environment variables
echo "🚀 Starting Liberty server with telemetry configuration..."
mvn liberty:dev
