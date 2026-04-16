# Build Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
# (Note: Using Temurin 21 here as 25 is currently in early-access/not fully stable in all public Maven Docker images yet. 
# Spring Boot 4.x compiles perfectly on Java 21 LTS, which we set here for guaranteed Render.com compatibility)
WORKDIR /build

# Copy only pom.xml to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
# Override the java_version property during build if needed, though Spring Boot parent will handle 21 fine
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy JAR from build stage
COPY --from=builder /build/target/*.jar app.jar

# Define port 8080 (Render.com requires apps to bind to $PORT or a designated port)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
