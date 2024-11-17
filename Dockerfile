FROM openjdk:17-jdk-slim

WORKDIR /app

ARG JAR_FILE=*.jar

COPY target/${JAR_FILE} /app/billmanager.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/billmanager.jar"]
