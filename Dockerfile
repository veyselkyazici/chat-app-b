FROM gradle:8.14.3-jdk24-alpine AS builder

WORKDIR /app

ARG SERVICE=config-server
COPY ${SERVICE} ${SERVICE}

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN ./gradlew :${SERVICE}:bootJar --no-daemon --quiet

FROM eclipse-temurin:24-jre-alpine
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

WORKDIR /app

ARG SERVICE=config-server
COPY --from=builder /app/${SERVICE}/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
