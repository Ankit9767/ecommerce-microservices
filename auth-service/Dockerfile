# ---------- Build Stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY . .

RUN mvn clean package \
    -pl auth-service \
    -am \
    -DskipTests


# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/auth-service/target/*.jar app.jar


# Create non-root user
RUN useradd -u 1001 -m spring \
    && chown -R spring:spring /app \
    && chmod 777 /tmp


# Run application as non-root user
USER spring


EXPOSE 8090

ENTRYPOINT ["java","-jar","app.jar"]