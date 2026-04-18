FROM eclipse-temurin:17-jre-alpine
WORKDIR /home/app
COPY build/libs/*SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
