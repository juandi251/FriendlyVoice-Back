# Dockerfile para Spring Boot en Render
FROM maven:3.9-eclipse-temurin-17 AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración Maven primero (para cachear dependencias)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar el proyecto
RUN mvn clean install -DskipTests

# Imagen final más ligera
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el JAR desde la imagen de build
COPY --from=build /app/target/back-friendlyvoice-*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variable de entorno para el puerto (Render la asigna)
ENV PORT=8080

# Ejecutar aplicación
ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=${PORT} app.jar"]

