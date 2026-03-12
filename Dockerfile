################ Build ################
# Build stage for the full multi-module project
FROM maven:3.9.12-eclipse-temurin-25-alpine AS build

WORKDIR /workspace

# Copy everything (keeps it simple for multi-module; rely on Docker cache)
COPY . .

# Build all modules (skip tests for faster image builds)
RUN mvn -T1C -DskipTests package \
    && mkdir -p /workspace/.artifacts /workspace/.bundles/plugins /workspace/.bundles/examples \
    && cp -f /workspace/fluxcord-core/target/*-shaded.jar /workspace/.artifacts/fluxcord.jar \
    && (cp -f /workspace/plugins/*/target/*.jar /workspace/.bundles/plugins/ 2>/dev/null || true) \
    && (cp -f /workspace/examples/plugins/*/target/*.jar /workspace/.bundles/examples/ 2>/dev/null || true)

################ Production ################
# Use a small JRE base with shell to run an entrypoint script
FROM eclipse-temurin:25.0.1_8-jre-alpine-3.22 AS production

ENV APP_DIR=/app \
    BUNDLES_DIR=/opt/fluxcord/bundles \
    JAVA_OPTS=""

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

## Core shaded jar
COPY --from=build /workspace/.artifacts/fluxcord.jar /app/fluxcord.jar

# Bundle available plugins (internal plugins)
RUN mkdir -p /opt/fluxcord/bundles/plugins /opt/fluxcord/bundles/examples

# Copy bundled plugin jars (empty dir is fine)
COPY --from=build /workspace/.bundles/plugins/ /opt/fluxcord/bundles/plugins/
COPY --from=build /workspace/.bundles/examples/ /opt/fluxcord/bundles/examples/

# Entrypoint script to optionally install/update plugins before launch
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

VOLUME ["/app/data", "/app/plugins"]

ENTRYPOINT ["/entrypoint.sh"]

# Docker healthcheck (ready file created by core after full startup)
# Docker healthcheck: HTTP ready endpoint exposed by the core
ENV HEALTH_PORT=8081
HEALTHCHECK --interval=10s --timeout=3s --retries=3 CMD curl -fsS http://127.0.0.1:${HEALTH_PORT}/readyz || exit 1
