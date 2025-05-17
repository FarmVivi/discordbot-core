################ Build ################
# Build stage for building the application for production
FROM maven:3.9.9-eclipse-temurin-17-alpine as build

# Create project directory (workdir)
WORKDIR /app

# Install maven dependency packages
COPY pom.xml .
RUN mvn -T 1C install && rm -rf target

# Add source code files to WORKDIR
ADD . .

# Build application
RUN ./build.sh

################ Production ################
# Creates a minimal image for production using distroless base image
# More info here: https://github.com/GoogleContainerTools/distroless
FROM gcr.io/distroless/java17-debian12:latest as production

# Create project directory (workdir)
WORKDIR /app

# Copy application binary from build stage to the production container
COPY --from=build /app/target/discordbot-core.jar /app

# Container start command for production
CMD ["discordbot-core.jar"]
