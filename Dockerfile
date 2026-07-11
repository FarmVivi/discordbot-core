################ Build ################
# Build stage for the full multi-module project
FROM maven:3.9.16-eclipse-temurin-25-alpine AS build

WORKDIR /workspace

# Copy everything (keeps it simple for multi-module; rely on Docker cache)
COPY . .

# Build all modules (skip tests for faster image builds)
RUN mvn -T1C -DskipTests package \
    && mkdir -p /workspace/.artifacts /workspace/.bundles/plugins /workspace/.bundles/examples \
    && cp -f /workspace/fluxcord-core/target/*-shaded.jar /workspace/.artifacts/fluxcord.jar \
    && (cp -f /workspace/plugins/*/target/*.jar /workspace/.bundles/plugins/ 2>/dev/null || true) \
    && (cp -f /workspace/examples/plugins/*/target/*.jar /workspace/.bundles/examples/ 2>/dev/null || true)

################ libdave (musl) ################
# JDAVE needs a native libdave.so, but upstream only ships glibc builds (needs GLIBC>=2.38).
# To stay on Alpine we compile libdave for musl ourselves and load it via -Djdave.library.path.
FROM alpine:3.22 AS libdave-build

# vcpkg on musl must use the system toolchain instead of prebuilt (glibc) binaries
ENV VCPKG_FORCE_SYSTEM_BINARIES=1

RUN apk add --no-cache build-base cmake ninja make git bash curl zip unzip tar \
    perl linux-headers nasm python3 pkgconf zlib-dev

WORKDIR /libdave
# Pin to the libdave version the JDAVE bindings expect (jdave 0.1.8 -> libdave v1.1.1).
# KEEP IN SYNC with jdave.version in the root pom.xml (see the note there when bumping).
RUN git clone --branch v1.1.1 --depth 1 --recursive https://github.com/MinnDevelopment/libdave.git . \
    && ./cpp/vcpkg/bootstrap-vcpkg.sh -disableMetrics
WORKDIR /libdave/cpp
RUN make cclean && make shared \
    && install -D "$(find build -name 'libdave.so' | head -1)" /out/libdave.so

################ Production ################
# Alpine JRE base. libdave.so is compiled for musl above and only needs libstdc++ at runtime.
FROM eclipse-temurin:25.0.3_9-jre-alpine-3.22 AS production

ENV APP_DIR=/app \
    BUNDLES_DIR=/opt/fluxcord/bundles \
    JAVA_OPTS=""

WORKDIR /app

# curl for healthcheck; libstdc++ is required by the native libdave.so
RUN apk add --no-cache curl libstdc++

## Core shaded jar
COPY --from=build /workspace/.artifacts/fluxcord.jar /app/fluxcord.jar

# Native DAVE library (musl build) + point JDAVE at it
COPY --from=libdave-build /out/libdave.so /opt/libdave/libdave.so
ENV JAVA_TOOL_OPTIONS="-Djdave.library.path=/opt/libdave/libdave.so"

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
