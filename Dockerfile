# Stage 1: Build the application
# We use a Maven image based on Eclipse Temurin (Standard for Java 17)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the lightweight runtime image
# We use the JRE (Runtime) version of Eclipse Temurin to keep the image small
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the jar file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]