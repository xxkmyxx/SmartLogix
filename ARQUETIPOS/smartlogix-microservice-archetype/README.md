# SmartLogix – Arquetipo Maven para Microservicios

Este arquetipo define la estructura base estándar para crear nuevos microservicios dentro del sistema SmartLogix. Incluye configuración de Spring Boot, seguridad JWT, JPA, documentación OpenAPI y cobertura de pruebas con JaCoCo.

## ¿Qué genera este arquetipo?

Al usarlo, se crea un proyecto con la siguiente estructura:

```
<artifactId>/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── <ServiceName>Application.java
    │   │   ├── controller/
    │   │   │   └── EjemploController.java
    │   │   └── service/
    │   │       ├── EjemploService.java
    │   │       └── EjemploServiceImpl.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/
            ├── <ServiceName>ApplicationTests.java
            └── service/
                └── EjemploServiceTest.java
```

## Requisitos

- Java 17+
- Maven 3.8+
- El arquetipo instalado en el repositorio local Maven

## Paso 1 – Instalar el arquetipo localmente

Desde la carpeta `smartlogix-microservice-archetype/`:

```bash
mvn install
```

Esto registra el arquetipo en tu repositorio Maven local (`~/.m2`).

## Paso 2 – Generar un nuevo microservicio

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.smartlogix \
  -DarchetypeArtifactId=smartlogix-microservice-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=com.smartlogix \
  -DartifactId=nombre-servicio \
  -Dversion=0.0.1-SNAPSHOT \
  -DserviceName=NombreServicio \
  -DserverPort=8084 \
  -DinteractiveMode=false
```

### Parámetros

| Parámetro | Descripción | Ejemplo |
|---|---|---|
| `artifactId` | Nombre del módulo (minúsculas) | `reportes` |
| `serviceName` | Nombre en PascalCase para las clases | `Reportes` |
| `serverPort` | Puerto del servidor | `8084` |

## Paso 3 – Ejecutar el proyecto generado

```bash
cd nombre-servicio
mvn spring-boot:run
```

## Patrones incluidos en el arquetipo

- **Strategy / Interface-Impl**: Separación entre `EjemploService` (interfaz) y `EjemploServiceImpl` (implementación), permitiendo intercambiar lógicas sin modificar el controlador.
- **Layered Architecture**: Capas controller → service → repository bien delimitadas.
- **Template Method** (vía Spring): El ciclo de vida de los beans sigue el patrón de Spring IoC.

## Microservicios generados con este arquetipo

| Microservicio | Puerto | Descripción |
|---|---|---|
| auth | 8081 | Autenticación y gestión de usuarios |
| inventario | 8082 | Control de stock y bodegas |
| pedidos | 8083 | Gestión de pedidos y estados |
| bff | 8080 | Backend For Frontend (proxy y agregación) |
