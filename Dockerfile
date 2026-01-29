# Use a Maven JDK image to build the app
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Use a lighter JDK image for runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/chatbot-0.0.1-SNAPSHOT.jar ./chatbot.jar

# Expose the port your app runs on
EXPOSE 10000

# Environment variable for OpenAI API key
ENV OPENAI_API_KEY=""

# Start the Spring Boot app
ENTRYPOINT ["java","-jar","chatbot.jar"]
