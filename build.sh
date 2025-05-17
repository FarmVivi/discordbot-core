#!/bin/sh
set -e

# Define build options
MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# Function to perform the build
perform_build() {
  echo "Building discordbot-core..."
  
  # Clean if needed but only when not in Docker (preserve Docker cache)
  if [ "$1" != "build-only" ]; then
    mvn clean
  fi
  
  # Package the application
  mvn package -DskipTests
  
  # Rename jar file for convenience
  cp target/discordbot-core-*.jar target/discordbot-core.jar
  
  echo "Build completed successfully!"
}

# Main execution logic
if [ "$1" = "run" ]; then
  # Build and run
  perform_build
  echo "Starting application..."
  java -jar target/discordbot-core.jar
elif [ "$1" = "build-only" ]; then
  # Just build (for Docker)
  perform_build build-only
else
  # Default: just build
  perform_build
fi
