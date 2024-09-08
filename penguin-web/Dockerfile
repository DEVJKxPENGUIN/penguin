FROM eclipse-temurin:21

ARG JAR_FILE=./build/libs/penguin-web-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar", "-Djava.security.egd=file:/dev/./urandom", "--spring.profiles.active=prod"]