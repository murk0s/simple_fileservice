FROM alpine/java:21-jdk AS builder
RUN mkdir -p /app
WORKDIR /app

COPY build.gradle gradlew  .
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew && ./gradlew --no-daemon clean shadowJar

FROM eclipse-temurin:21-jre-alpine AS runtime
COPY --from=builder app/build/libs/*.jar app/app.jar

CMD ["java", "-cp", "/app/app.jar", "Application"]