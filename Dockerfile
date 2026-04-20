# Étape 1 : Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Créer le dossier uploads (essentiel !)
RUN mkdir -p /app/uploads/campaigns /app/uploads/profiles && chmod -R 755 /app/uploads

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]