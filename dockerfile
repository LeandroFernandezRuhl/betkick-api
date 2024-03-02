FROM openjdk:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file
COPY build/libs/BetKickAPI-0.0.1-SNAPSHOT.jar betkick.jar

# Set the entry point
ENTRYPOINT ["java", "-jar", "betkick.jar"]
