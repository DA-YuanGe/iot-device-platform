FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
COPY protocol/pom.xml protocol/pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY service/pom.xml service/pom.xml
COPY simulator/pom.xml simulator/pom.xml

COPY protocol/src protocol/src
COPY gateway/src gateway/src
COPY service/src service/src
COPY simulator/src simulator/src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

ARG MODULE
WORKDIR /app

COPY --from=builder /build/${MODULE}/target/${MODULE}-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
