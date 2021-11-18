################ Build & Dev ################
# Build stage will be used:
# - for building the application for production
# - as target for development (see devspace.yaml)
FROM maven:3-openjdk-11 as build

# Create project directory (workdir)
WORKDIR /app

# Remove http blocker
COPY settings.xml /usr/share/maven/conf/

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
# Creates a minimal image for production using distroless base image
# More info here: https://github.com/GoogleContainerTools/distroless
FROM gcr.io/distroless/java:11 as production

# Environnement variables
ENV DISCORD_TOKEN="" SPOTIFY_ID="" SPOTIFY_TOKEN="" BOT_CMD_PREFIX="" BOT_CMD_ADMINS=""

# Copy application binary from build/dev stage to the distroless container
COPY --from=build /app/target/main.jar /

# Container start command for production
CMD ["/main.jar"]
