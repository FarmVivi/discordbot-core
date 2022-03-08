#!/bin/sh

# Set maven build options
MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# Remove old .jar files
rm -f target/*.jar

# Build jar file
mvn compile package

# Rename jar file
cp target/discordbot-*.jar target/discordbot.jar

# Start application if `run` argument is passed
if [ $1 = "run" ]; then
  java -jar target/discordbot.jar
fi
