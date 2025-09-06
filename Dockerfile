# Use Eclipse Temurin JDK 21 for build
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy Gradle wrapper and dependencies first for better caching
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src/ src/

# Build the application with our custom command that works
RUN ./gradlew clean bootJar -Pproduction -x test -x check --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install curl for health checks (optional)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built jar
COPY --from=build /app/build/libs/inventory-system.jar app.jar

# Expose port (Railway will override with $PORT)
EXPOSE 8080

# Run with optimized memory for Railway Hobby Plan (512MB available)
CMD ["java", "-Dspring.profiles.active=prod", "-Xms128m", "-Xmx400m", "-XX:+UseG1GC", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-XX:MaxMetaspaceSize=128m", "-jar", "app.jar"]