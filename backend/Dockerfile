# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/target/material-management-1.0.0.jar app.jar

# Not exposed to the host - only reachable from the frontend/nginx container
# over the compose network. -Xmx caps the heap: several other JVMs already
# run on this server.
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
