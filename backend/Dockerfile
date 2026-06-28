# ==========================================
# Build Stage
# ==========================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first for faster incremental builds
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Compile and package
COPY src ./src
RUN mvn -B clean package -DskipTests

# ==========================================
# Runtime Stage — slim JRE image
# ==========================================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Non-root user (least-privilege)
RUN groupadd --system bankgroup && useradd --system --gid bankgroup bankuser
USER bankuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Uses the default (in-memory H2) profile per DEC-021.
ENTRYPOINT ["java", "-jar", "app.jar"]
