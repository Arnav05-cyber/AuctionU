FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /home/app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY app app

RUN chmod +x gradlew
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /home/app
COPY --from=build /home/app/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
