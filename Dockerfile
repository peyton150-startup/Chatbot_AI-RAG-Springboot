# -----------------------------
# Build stage
# -----------------------------
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Step 1: Copy pom.xml first for caching dependencies
COPY pom.xml .

# Step 2: Pre-download dependencies
RUN mvn dependency:go-offline -B

# Step 3: Copy the full source code
COPY src ./src

# Step 4: Test and build the project
ENV MAVEN_OPTS="-Xmx1024m" 
RUN mvn clean verify -B -X

# -----------------------------
# Runtime stage
# -----------------------------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
