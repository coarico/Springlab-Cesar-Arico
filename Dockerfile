# Usa la imagen de OpenJDK 17
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Copia los archivos de configuración de Gradle
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Descarga las dependencias
RUN chmod +x ./gradlew
RUN ./gradlew dependencies

# Copia el código fuente
COPY src ./src

# Construye la aplicación
RUN ./gradlew clean build -x test

# Puerto de la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "build/libs/spring-lab-0.0.1-SNAPSHOT.jar", "--server.port=$PORT"]
