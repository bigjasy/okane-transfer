# Build WAR (application.properties from example — runtime config via env in entrypoint)
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN cp src/main/resources/application.properties.example src/main/resources/application.properties \
    && mvn -q package -DskipTests

# Run on Tomcat (ROOT context → API at /api/v1)
FROM tomcat:10.1-jdk17-temurin-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends postgresql-client python3 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/okane-transfer-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
COPY schema.sql seed_users.sql /app/db/
COPY docker-entrypoint.sh /entrypoint.sh
RUN sed -i 's/\r$//' /entrypoint.sh && chmod +x /entrypoint.sh

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["/entrypoint.sh"]
