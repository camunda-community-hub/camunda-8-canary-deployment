# docker build -t pierre-yves-monnet/processautomator:1.5.0 .
# JDK 17: openjdk:17-alpine
# JDK 21: alpine/java:21-jdk
FROM eclipse-temurin:21-jdk-alpine
EXPOSE 8095
COPY target/canary-deployment-*-exec.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]

