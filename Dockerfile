# Use a lightweight JDK base image
FROM eclipse-temurin:17-jdk-jammy as builder

# Set work directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]