FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /home/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /home/app
COPY --from=build /home/app/build/libs/*.jar app.jar
EXPOSE 9005
ENTRYPOINT ["java", "-jar", "app.jar"]
