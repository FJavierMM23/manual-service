# ---------- Etapa de build ----------
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Cachea las dependencias en su propia capa (solo se reinstalan si cambia el pom.xml)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ---------- Etapa de runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Usuario no-root, mismo patrón que ai-service
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=builder /build/target/manual-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
    CMD wget -qO- http://localhost:8080/api/health | grep -q '"manualService":"ok"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]