# ---- Stage 1: Build the JAR ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cache-friendly)
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn -q clean package -DskipTests


# ---- Stage 2: Run the JAR ----
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
