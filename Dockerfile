# Use a stable Maven + JDK image
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Step 1: Copy only pom.xml to leverage Docker cache
COPY pom.xml .

# Step 2: Pre-download all dependencies
RUN mvn dependency:go-offline -B

# Step 3: Copy source code
COPY src ./src

# Step 4: Build the project (clean install)
RUN mvn clean install -B -DskipTests

# -----------------------------
# Optional: Create a smaller runtime image
# -----------------------------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy built JAR from previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Command to run the app
ENTRYPOINT ["java","-jar","app.jar"]
