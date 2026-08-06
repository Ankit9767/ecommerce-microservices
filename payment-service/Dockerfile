# ---------- Build Stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY . .

RUN mvn clean package \
    -pl payment-service \
    -am \
    -DskipTests


# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/payment-service/target/*.jar app.jar


# Create non-root user and group
RUN groupadd -g 1001 spring \
    && useradd -u 1001 -g spring spring \
    && chown -R spring:spring /app


# Run application as non-root
USER spring:spring

EXPOSE 8086

ENTRYPOINT ["java","-jar","app.jar"]