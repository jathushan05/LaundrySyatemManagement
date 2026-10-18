# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

FROM tomcat:10.1-jdk17-temurin

ENV CATALINA_OPTS="-Djava.security.egd=file:/dev/urandom -Dfile.encoding=UTF-8"

RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /workspace/target/laundry-management.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
  CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080"]

CMD ["catalina.sh", "run"]
