# =====================================================================
# Perfumería YVI - Imagen Docker (etapas BUILD y RELEASE de DevOps)
# Etapa 1: compila y empaqueta el WAR con Maven.
# Etapa 2: imagen liviana solo con Java 21 (JRE) para ejecutar.
# =====================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.title="perfumeriaYVI" \
      org.opencontainers.image.description="Inventario y ventas Perfumería YVI (MVVM)"

# La aplicación no corre como root dentro del contenedor
RUN addgroup -S yvi && adduser -S yvi -G yvi
WORKDIR /app
COPY --from=build /build/target/perfumeriaYVI.war app.war
RUN mkdir -p /app/logs && chown -R yvi:yvi /app
USER yvi

ENV SPRING_PROFILES_ACTIVE=prod \
    MANAGEMENT_PORT=8081 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=America/Bogota"
# 8080: aplicación · 8081: salud y métricas (solo red interna de Docker, no se publica)
EXPOSE 8080 8081

# OPERATE / MONITOR: Docker revisa la salud de la aplicación cada 30 s
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health | grep -q '"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.war"]
