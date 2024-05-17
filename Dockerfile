# syntax = docker/dockerfile:1.2
#
# Build stage
#
FROM maven:3.8.6-openjdk-18 AS build
COPY . .
RUN mvn clean package assembly:single -DskipTests
#
# Package stage
#
FROM openjdk:17-jdk-slim
RUN java -version

COPY --from=build /app/target/javalin-deploy-1.0-SNAPSHOT-jar-with-dependencies.jar logistica.jar

RUN ls -l

# ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-classpath","logistica.jar","ar.edu.utn.dds.k3003.app.WebApp"]

