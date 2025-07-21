################ Build ################
# Build stage for building the application for production
FROM maven:3.9-eclipse-temurin-11-alpine as build

# Create project directory
WORKDIR /app

# Copy only pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .

# Download dependencies and cache them (but don't build yet)
RUN mvn dependency:go-offline -B

# Copy source files
COPY src ./src
COPY build.sh ./

# Build the application in one step
RUN chmod +x ./build.sh && \
    ./build.sh build-only

################ Production ################
# Creates a minimal image for production using distroless base image
# More info here: https://github.com/GoogleContainerTools/distroless
FROM gcr.io/distroless/java17-debian12:latest as production

# Create project directory
WORKDIR /app

# Copy only the built jar file
COPY --from=build /app/target/discordbot-core.jar ./discordbot-core.jar

# Set the entry point
ENTRYPOINT ["java", "-jar", "discordbot-core.jar"]
