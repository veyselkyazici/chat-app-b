# 1. Stage: Build
FROM gradle:8.10-jdk17-alpine AS builder

WORKDIR /app

# Root projeyi kopyala
COPY . .

# ARG ile hangi servisin build edileceğini seçiyoruz
ARG SERVICE=config-server

# İlgili module’den jar üret
RUN gradle :${SERVICE}:bootJar --no-daemon

# 2. Stage: Run
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# ARG tekrar tanımlanmalı (stage'ler arası izole)
ARG SERVICE=config-server

# Doğru module’den jar’ı al
COPY --from=builder /app/${SERVICE}/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
