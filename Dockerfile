FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from Jenkins
COPY target/*.jar app.jar

# Environment variables with defaults
ENV SPRING_APPLICATION_NAME=backend \
    SERVER_PORT=8081 \
    SPRING_JPA_HIBERNATE_DDL_AUTO=update \
    SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/dental \
    SPRING_DATASOURCE_USERNAME=placeholder \
    SPRING_DATASOURCE_PASSWORD=placeholder \
    SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver \
    SPRING_JPA_SHOW_SQL=false \
    APP_CORS_ALLOWED_ORIGINS=* \
    APP_RESET_PASSWORD_LINK=http://localhost:3000/reset-password \
    SPRING_APP_JWTSECRET=defaultsecret \
    SPRING_APP_JWTEXPIRATIONMS=86400000 \
    SPRING_APP_JWTCOOKIENAME=dn-dental-clinic \
    SPRING_MAIL_HOST=smtp.gmail.com \
    SPRING_MAIL_PORT=587 \
    SPRING_MAIL_USERNAME=placeholder \
    SPRING_MAIL_PASSWORD=placeholder \
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true \
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true \
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED=true \
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTIONTIMEOUT=5000 \
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_TIMEOUT=5000 \
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_WRITETIMEOUT=5000 \
    AWS_ACCESSKEYID=placeholder \
    AWS_SECRETKEY=placeholder \
    AWS_REGION=eu-north-1 \
    AWS_S3_BUCKET=placeholder

EXPOSE 8081

COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]