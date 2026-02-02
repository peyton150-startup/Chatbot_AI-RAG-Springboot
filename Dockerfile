# Build image
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean install -B -X

# Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/chatbot-*.jar ./chatbot.jar


EXPOSE 10000
ENV OPENAI_API_KEY=""

ENTRYPOINT ["java","-jar","chatbot.jar"]
<mirror>
    <id>aliyun</id>
    <name>aliyun maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
    <mirrorOf>*</mirrorOf>
</mirror>
