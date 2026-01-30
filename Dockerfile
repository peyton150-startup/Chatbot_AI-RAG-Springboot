# Build image
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvnw dependency:go-offline

COPY src ./src
RUN mvnw clean package -DskipTests

# Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/chatbot-0.0.1-SNAPSHOT.jar ./chatbot.jar

EXPOSE 10000
ENV OPENAI_API_KEY=""

ENTRYPOINT ["java","-jar","chatbot.jar"]
