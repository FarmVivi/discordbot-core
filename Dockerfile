################ Build & Dev ################
# Build stage will be used:
# - for building the application for production
# - as target for development (see devspace.yaml)
FROM maven:3-openjdk-11-slim as build

# Create project directory (workdir)
WORKDIR /app

# Install maven dependency packages
COPY pom.xml .
RUN mvn -T 1C install && rm -rf target

# Add source code files to WORKDIR
ADD . .

# Build application
RUN ./build.sh

# Container start command for development
# Allows DevSpace to restart the dev container
# It is also possible to override this in devspace.yaml via images.*.cmd
CMD ["./build.sh", "run"]


################ Production ################
# Creates a minimal image for production
FROM adoptopenjdk/openjdk11:alpine-jre as production

# Environnement variables
ENV DISCORD_TOKEN="" SPOTIFY_ID="" SPOTIFY_TOKEN="" CMD_PREFIX="" CMD_ADMINS="" RADIO_PATH="" FEATURES="MUSIC"

# Create directory for application binary
RUN mkdir /opt/app

# Copy application binary from build/dev stage to the production container
COPY --from=build /app/target/main.jar /opt/app

# Container start command for production
CMD ["java", "-jar", "/opt/app/main.jar"]
