FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/Football-updates-0.0.1-SNAPSHOT.jar Football-updates-0.0.1-SNAPSHOT.jar
EXPOSE 4000
CMD ["java", "-jar", "Football-updates-0.0.1-SNAPSHOT.jar"]