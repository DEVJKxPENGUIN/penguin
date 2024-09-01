FROM eclipse-temurin:21

ARG JAR_FILE=./build/libs/penguin-web-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","--spring.profiles.active=prod","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]