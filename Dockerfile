# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the jar file, skipping tests to save time during build
RUN mvn clean package -DskipTests

# Stage 2: Create the lightweight runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy the jar file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]