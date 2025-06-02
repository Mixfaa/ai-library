FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn ./.mvn
COPY src ./src
ENV MAVEN_OPTS="--enable-preview"
RUN mvn package -Pproduction -DskipTests

FROM eclipse-temurin:21
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java",  "-jar","--enable-preview", "app.jar"]