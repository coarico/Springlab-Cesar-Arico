# Multi-stage build para optimizar el tamaño y manejar problemas de red
# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de Gradle
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Dar permisos y descargar dependencias con reintentos
RUN chmod +x ./gradlew && \
    ./gradlew dependencies --no-daemon --refresh-dependencies || \
    ./gradlew dependencies --no-daemon --refresh-dependencies || \
    ./gradlew dependencies --no-daemon --refresh-dependencies

# Copiar código fuente
COPY src ./src

# Construir aplicación
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar solo el JAR construido desde el stage anterior
COPY --from=builder /app/build/libs/*.jar app.jar

# Copiar applicationRender.yml para Render
COPY --from=builder /app/src/main/resources/applicationRender.yml /app/application.yml

# Puerto de la aplicación (Render asigna dinámicamente)
EXPOSE 8080

# Comando para ejecutar la aplicación con el archivo de configuración de Render
# Render proporciona PORT como variable de entorno
CMD ["sh", "-c", "java -jar app.jar --spring.config.location=file:/app/application.yml --server.port=${PORT:-8080}"]
