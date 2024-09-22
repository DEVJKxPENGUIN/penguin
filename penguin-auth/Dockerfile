FROM eclipse-temurin:21-alpine

ARG JAR_FILE=./build/libs/penguin-auth-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=prod","app.jar"]