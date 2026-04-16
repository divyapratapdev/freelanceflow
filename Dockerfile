# ─────────────────────────────────────────────────────────
# Build Stage
# ─────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app
# Copy pom and wait for dependecies to be resolved
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and compile
COPY src ./src
RUN mvn clean package -DskipTests

# ─────────────────────────────────────────────────────────
# Production Stage
# ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Non-root user for security (Industrial-grade best practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/freelanceflow-1.0.0.jar app.jar

# Configuration via environment variables
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
