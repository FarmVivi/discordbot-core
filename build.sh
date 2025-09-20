#!/bin/sh
set -e

# Define build options
MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# Function to perform the build
perform_build() {
  echo "Building Fluxcord multi-module project..."
  
  # Clean if needed but only when not in Docker (preserve Docker cache)
  if [ "$1" != "build-only" ]; then
    mvn clean
  fi
  
  # Package the application
  mvn package -DskipTests
  
  # Rename jar file for convenience (core module contains the main application)
  cp fluxcord-core/target/fluxcord-core-*.jar target/fluxcord.jar
  
  echo "Build completed successfully!"
}

# Function to ensure target directory exists
ensure_target() {
  if [ ! -d target ]; then
    mkdir target
  fi
}

# Main execution logic
if [ "$1" = "run" ]; then
  # Build and run
  ensure_target
  perform_build
  echo "Starting application..."
  java -jar target/fluxcord.jar
elif [ "$1" = "build-only" ]; then
  # Just build (for Docker)
  ensure_target
  perform_build build-only
else
  # Default: just build
  ensure_target
  perform_build
fi
