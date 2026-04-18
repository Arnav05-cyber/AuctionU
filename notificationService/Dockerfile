FROM eclipse-temurin:17-jre-alpine
WORKDIR /home/app
COPY build/libs/*SNAPSHOT.jar app.jar
EXPOSE 9005
ENTRYPOINT ["java", "-jar", "app.jar"]
