FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from Jenkins
COPY target/*.jar app.jar

# Copy entrypoint script
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Environment variables will be provided by docker-compose
EXPOSE 8081

ENTRYPOINT ["/app/entrypoint.sh"]
