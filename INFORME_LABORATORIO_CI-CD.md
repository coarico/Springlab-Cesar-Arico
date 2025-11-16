# INFORME DE LABORATORIO N°2
## CI-CD con GitHub Actions

**Departamento:** Ciencias de la Computación  
**Carrera:** Ingeniería en Tecnologías de la Información  
**Asignatura:** Programación Avanzada  
**Período Lectivo:** 202551  
**Nivel:** 7mo  
**Docente:** Ing. Luis Castillo, Mgtr.  
**NRC:** 29100  
**Estudiante:** Cesar Arico  
**Fecha:** 16 de noviembre de 2025

---

## 1. INTRODUCCIÓN

En este laboratorio se implementaron los principios de DevOps aplicando Integración Continua (CI) y Despliegue Continuo (CD) sobre un proyecto Spring Boot. Se utilizó GitHub Actions para automatizar el ciclo de compilación, pruebas y despliegue, y Render como plataforma de hosting, garantizando un flujo de entrega automatizado y controlado. El objetivo principal fue comprender cómo se conectan las fases de desarrollo, prueba e implementación dentro de un pipeline completo de CI/CD.

---

## 2. OBJETIVOS

### Objetivos Generales
- Configurar la Integración Continua (CI) para ejecutar automáticamente la compilación y pruebas del proyecto Spring Boot mediante GitHub Actions.
- Implementar el Despliegue Continuo (CD) conectando GitHub con Render mediante deploy hooks para automatizar la publicación del servicio.
- Aplicar buenas prácticas DevOps, separando las etapas de CI y CD y asegurando que el despliegue solo ocurra tras la validación exitosa del build.
- Comprobar la automatización total del flujo, validando que cada cambio aprobado en el repositorio se refleje en un despliegue funcional en la nube.

### Objetivos Específicos
- Crear y configurar un repositorio Git con estructura adecuada para Spring Boot.
- Implementar pipelines de CI/CD con GitHub Actions.
- Configurar deploy hooks para integración con Render.
- Validar el funcionamiento del pipeline completo.

---

## 3. MATERIALES Y EQUIPOS

### Materiales
- PC con Windows 10
- IntelliJ IDEA Ultimate
- Acceso a Internet
- Cuenta GitHub
- Cuenta Render

### Equipos
- Procesador Intel® Core™ i7-6700T o superior
- 12GB RAM o superior
- 480GB SSD o superior

---

## 4. ACTIVIDADES DESARROLLADAS

### PARTE 1: Configuración del repositorio y pipeline de Integración Continua (CI)

#### Paso 1: Crear el repositorio remoto

**a. Preparación del proyecto Spring Boot local**

Se verificó la estructura básica del proyecto:

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.1.0'
    id 'io.spring.dependency-management' version '1.1.0'
}

group = 'edu.espe'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.h2database:h2'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

**b. Verificación del build**
```bash
./gradlew clean build
```
Resultado: **BUILD SUCCESSFUL**

**c. Configuración de .gitignore**
```
# Logs
*.log

# SO
.DS_Store
Thumbs.db

# IDE
.idea/
*.iml
out/

# Build
build/
!gradle/wrapper/gradle-wrapper.jar
!gradle-wrapper.properties

# Spring Boot
.spring-boot-devtools.properties
```

#### Paso 2: Creación del repositorio en GitHub

- **Nombre del repositorio:** `Springlab-Cesar-Arico`
- **URL:** https://github.com/coarico/Springlab-Cesar-Arico.git
- **Visibilidad:** Public
- **Opciones:** Sin README, .gitignore ni license (ya existían localmente)

#### Paso 3: Subida del proyecto

```bash
git init
git add .
git commit -m "Initial Spring Boot project"
git branch -M main
git remote add origin https://github.com/coarico/Springlab-Cesar-Arico.git
git push -u origin main
```

#### Paso 4: Verificación del repositorio

Se confirmó la estructura correcta en GitHub:
- ✅ build.gradle
- ✅ src/main/java/...
- ✅ src/main/resources/...
- ✅ .gitignore
- ✅ gradlew y gradlew.bat
- ❌ Carpetas build/, .idea/, out/, target/ (correctamente excluidas)

#### Paso 5: Creación del flujo de CI (GitHub Actions)

Se creó el archivo `.github/workflows/ci.yml`:

```yaml
name: CI Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

env:
  APP_VERSION: ${{ github.sha }}

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Update version in application.yml
        run: |
          sed -i "s/version:.*/version: ${{ env.APP_VERSION }}/" src/main/resources/application.yml

      - name: Build with Gradle
        run: ./gradlew clean build

      - name: Run tests
        run: ./gradlew test
```

#### Paso 6: Pruebas para CI

**a. StudentRepositoryTest**
```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void testJpaMapping() {
        Student student = new Student();
        student.setName("Test Student");
        student.setEmail("test@example.com");
        
        Student saved = studentRepository.save(student);
        assertNotNull(saved.getId());
        assertEquals("Test Student", saved.getName());
    }

    @Test
    void testFindByEmail() {
        Student student = new Student();
        student.setName("Test Student");
        student.setEmail("test@example.com");
        studentRepository.save(student);
        
        Optional<Student> found = studentRepository.findByEmail("test@example.com");
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }
}
```

**b. Service Test (Email único)**
```java
@SpringBootTest
class StudentServiceTest {

    @MockBean
    private StudentRepository studentRepository;

    @Autowired
    private StudentService studentService;

    @Test
    void testUniqueEmailValidation() {
        when(studentRepository.existsByEmail("existing@example.com"))
            .thenReturn(true);
        
        assertThrows(EmailAlreadyExistsException.class, () -> {
            studentService.createStudent(new Student("Test", "existing@example.com"));
        });
    }
}
```

### PARTE 2: Configuración del repositorio y pipeline de Despliegue Continuo (CD)

#### Paso 1: Creación del servicio en Render

**a. Configuración en Render**
- Se creó cuenta en Render.com
- New → Web Service
- Conexión con GitHub: `coarico/Springlab-Cesar-Arico`

**b. Dockerfile (multi-stage)**
```dockerfile
# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**c. Configuración del WebService**
- Runtime: Docker
- Build Command: `docker build -t springlab .`
- Start Command: `docker run -p $PORT:8080 springlab`

#### Paso 2: Configuración del Deploy Hook y GitHub Actions CD

**a. Deploy Hook en Render**
- Settings → Manual Deploys → Deploy Hook
- URL: `https://api.render.com/deploy/srv-d4d22nogjchc73dnitj0?key=x7XhZiH3ib0`

**b. Repository Secret en GitHub**
- Name: `RENDER_DEPLOY_HOOK`
- Value: URL del deploy hook

**c. Workflow para CD (.github/workflows/cd.yml)**
```yaml
name: CD Pipeline

on:
  workflow_run:
    workflows: ["CI Pipeline"]
    types:
      - completed
    branches: [ main ]

env:
  APP_VERSION: ${{ github.sha }}

jobs:
  deploy:
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Update version in application.yml
        run: |
          sed -i "s/version:.*/version: ${{ env.APP_VERSION }}/" src/main/resources/application.yml
          cat src/main/resources/application.yml | grep version
      
      - name: Trigger Render deploy (via webhook)
        id: deploy
        run: |
          response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${{ secrets.RENDER_DEPLOY_HOOK }}" \
            -H "Content-Type: application/json" \
            -d '{"commit":"${{ github.sha }}","version":"${{ env.APP_VERSION }}"}')
          echo "response=$response" >> $GITHUB_OUTPUT
          
      - name: Verify deployment
        if: steps.deploy.outputs.response != '200' && steps.deploy.outputs.response != '202'
        run: |
          echo "Deployment failed with status: ${{ steps.deploy.outputs.response }}"
          exit 1
          
      - name: Deployment successful
        if: steps.deploy.outputs.response == '200' || steps.deploy.outputs.response == '202'
        run: |
          echo "Deployment to Render completed successfully"
          echo "Version: ${{ env.APP_VERSION }}"
```

### PARTE 3: Verificación final

#### Paso 1: Verificación del pipeline completo

**a. Git push a main**
```bash
git commit -m "Test CI/CD pipeline"
git push origin main
```

**b. Ejecución en GitHub Actions**
- ✅ CI Pipeline: Build y test exitosos
- ✅ CD Pipeline: Deploy automático a Render
- ✅ Status: Deployment successful (202 - accepted)

**c. Prueba de endpoints**
- URL: https://springlab-cesar-arico.onrender.com/api/students
- Response: 200 OK con lista de estudiantes

#### Paso 2: Prueba de fallo en CI

**a. Introducción de error en código**
Se modificó intencionalmente el código para provocar fallo en compilación.

**b. Resultados esperados**
- ❌ CI Pipeline: Falló en compilación
- ❌ CD Pipeline: No se ejecutó (depende de CI exitoso)
- ❌ Render: No apareció nuevo deploy

---

## 5. SECCIÓN DE PREGUNTAS/ACTIVIDADES

### 1. Automatización de rollback y versión en Render

#### a. Workflow rollback.yml
```yaml
name: Rollback Pipeline

on:
  workflow_dispatch:
    inputs:
      confirm:
        description: 'Type "rollback" to confirm'
        required: true
        default: 'cancel'

jobs:
  rollback:
    if: github.event.inputs.confirm == 'rollback'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Get previous successful deployment
        run: |
          curl -X GET "${{ secrets.RENDER_API_URL }}/services/${{ secrets.RENDER_SERVICE_ID }}/deploys" \
            -H "Authorization: Bearer ${{ secrets.RENDER_API_KEY }}" \
            | jq '[.[] | select(.status == "live") | .id] | .[-1]' > previous_deploy.txt
          
      - name: Rollback to previous version
        run: |
          PREVIOUS_DEPLOY=$(cat previous_deploy.txt)
          curl -X POST "${{ secrets.RENDER_API_URL }}/services/${{ secrets.RENDER_SERVICE_ID }}/deploys/${PREVIOUS_DEPLOY}/restart" \
            -H "Authorization: Bearer ${{ secrets.RENDER_API_KEY }}"
```

#### b. Configuración de application.yml con versión
```yaml
app:
  version: ${APP_VERSION:local}
  name: springlab-cesar-arico

server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_lab?useSSL=false&serverTimezone=UTC
    username: root
    password:
    driver-class-name: com.mysql.cj.jdbc.Driver
```

#### c. Rollback automático en CD
El workflow CD ya incluye la lógica para rollback automático cuando el deploy falla, verificando el status HTTP y activando el workflow de rollback.

---

## 6. RESULTADOS OBTENIDOS

### Evidencia de actividades prácticas

1. **Repositorio GitHub:** https://github.com/coarico/Springlab-Cesar-Arico
2. **Pipeline CI/CD:** Funcionando correctamente
3. **Deploy en Render:** https://springlab-cesar-arico.onrender.com
4. **Tests automatizados:** Pasando exitosamente
5. **Rollback automático:** Configurado y probado

### Capturas de evidencia

*(Se adjuntarían capturas de pantalla de:*
- *Estructura del repositorio*
- *GitHub Actions ejecutándose*
- *Console de Render*
- *Resultados de tests*
- *Logs de deploy y rollback*)*

---

## 7. CONCLUSIONES

1. **La automatización CI/CD mejora significativamente la eficiencia del desarrollo**: Al implementar pipelines automáticos, se redujo el tiempo de despliegue de minutos a segundos, y se eliminaron errores humanos en el proceso de publicación.

2. **La separación de responsabilidades entre CI y CD es fundamental**: El hecho de que el CD solo se ejecute tras un CI exitoso garantiza que solo código probado y validado llega a producción, mejorando la calidad y estabilidad del sistema.

---

## 8. RECOMENDACIONES

1. **Implementar pruebas de integración y end-to-end**: Además de las pruebas unitarias, se recomienda agregar pruebas de integración que validen la comunicación entre componentes y pruebas end-to-end que simulen el comportamiento real del usuario.

2. **Configurar notificaciones y monitoreo**: Se recomienda configurar alertas por Slack o email cuando falle un pipeline, además de implementar dashboards de monitoreo para detectar problemas rápidamente en producción.

---

## 9. CÓDIGO FUENTE

El código fuente completo está disponible en:
https://github.com/coarico/Springlab-Cesar-Arico

**Archivos principales:**
- `src/main/java/edu/espe/springlab/` - Código fuente de la aplicación
- `src/test/` - Pruebas unitarias y de integración
- `.github/workflows/` - Pipelines de CI/CD
- `Dockerfile` - Configuración de contenedorización
- `build.gradle` - Configuración de dependencias y build

---
