FROM eclipse-temurin:17-jre-alpine
WORKDIR /home/app
COPY app/build/libs/app.jar app.jar
EXPOSE 9820
ENTRYPOINT ["java", "-jar", "app.jar"]
