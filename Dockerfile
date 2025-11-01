# Dockerfile para Spring Boot en Render
FROM maven:3.9-eclipse-temurin-17 AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivo pom.xml primero (para cachear dependencias)
COPY pom.xml .

# Descargar dependencias (esto se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar el proyecto y crear el JAR
RUN mvn clean package -DskipTests

# Imagen final más ligera (solo JRE, no JDK completo)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el JAR desde la imagen de build
COPY --from=build /app/target/back-friendlyvoice-*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variable de entorno para el puerto (Render la asigna automáticamente)
ENV PORT=8080

# Ejecutar aplicación usando el puerto de la variable de entorno
ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=${PORT} app.jar"]

