# Production image (Railway/GHCR). Local development uses Dockerfile.dev.
FROM eclipse-temurin:24-jdk-alpine AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# dependencies in their own layer: only rebuilt when pom.xml changes
COPY pom.xml .
RUN ./mvnw -B dependency:go-offline

# tests run in CI before the image is built
COPY src ./src
RUN ./mvnw -B package -DskipTests && cp target/*.jar app.jar


FROM eclipse-temurin:24-jre-alpine

WORKDIR /app
RUN addgroup -S homie && adduser -S homie -G homie
COPY --from=build /app/app.jar app.jar
USER homie

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
    CMD wget -qO- "http://localhost:${PORT:-8080}/health" || exit 1

# MaxRAMPercentage: size the heap from the container's memory limit
ENTRYPOINT [ "java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar" ]
