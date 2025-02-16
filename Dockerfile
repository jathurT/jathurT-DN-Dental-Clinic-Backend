FROM openjdk:17-jdk-slim as build

WORKDIR /app

# Copy maven files first for better caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR file
COPY --from=build /app/target/*.jar app.jar

# Environment variables with defaults
ENV SPRING_APPLICATION_NAME=myapp
ENV SERVER_PORT=8081
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=update
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/dental-clinic-db
ENV SPRING_DATASOURCE_USERNAME=placeholder
ENV SPRING_DATASOURCE_PASSWORD=placeholder
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
ENV SPRING_JPA_SHOW_SQL=false
ENV APP_CORS_ALLOWED_ORIGINS=*
ENV APP_RESET_PASSWORD_LINK=http://localhost:3000/reset-password
ENV SPRING_APP_JWTSECRET=defaultsecret
ENV SPRING_APP_JWTEXPIRATIONMS=86400000
ENV SPRING_APP_JWTCOOKIENAME=dn-dental-clinic
ENV SPRING_MAIL_HOST=smtp.gmail.com
ENV SPRING_MAIL_PORT=587
ENV SPRING_MAIL_USERNAME=placeholder
ENV SPRING_MAIL_PASSWORD=placeholder
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED=true
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTIONTIMEOUT=5000
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_TIMEOUT=5000
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_WRITETIMEOUT=5000
ENV AWS_ACCESSKEYID=placeholder
ENV AWS_SECRETKEY=placeholder
ENV AWS_REGION=us-east-1
ENV AWS_S3_BUCKET=placeholder

EXPOSE 8080

# Use an entrypoint script to load .env file if it exists
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]