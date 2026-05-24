# SmartLogix – Microservicio de Autenticación

Gestiona el registro, login y validación de tokens JWT para todos los usuarios del sistema SmartLogix.

## Tecnologías

- Java 17 + Spring Boot 3.5
- Spring Security + JWT (Auth0)
- Spring Data JPA + MySQL
- JaCoCo (cobertura de pruebas)

## Requisitos

- Java 17+
- Maven 3.8+
- MySQL 8+

## Configuración

Editar `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smartlogix_auth
spring.datasource.username=root
spring.datasource.password=tu_password

jwt.secret=smartlogix-secret-key
jwt.expiration=86400000
jwt.prefix=Bearer
jwt.header=Authorization
```

## Ejecución

```bash
mvn spring-boot:run
```

El servicio queda disponible en `http://localhost:8081`.

## Endpoints principales

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/login` | Login — retorna token JWT |
| POST | `/api/auth/register` | Registro de usuario |
| GET | `/api/auth/validate` | Validación de token |
| GET | `/api/usuarios` | Listar usuarios (ADMIN) |

## Pruebas unitarias

```bash
mvn test
```

El reporte de cobertura JaCoCo se genera en `target/site/jacoco/index.html`.

```bash
mvn test jacoco:report
```

## Patrones de diseño aplicados

- **Strategy**: `AuthService` (interfaz) / `AuthServiceImpl` (implementación) — permite intercambiar la lógica de autenticación sin afectar el controlador.
- **Builder**: construcción de `Usuario` y `LoginResponse` con Lombok `@Builder`.
- **Template Method** (vía Spring Security): el ciclo de filtrado HTTP sigue el patrón de cadena de filtros predefinido, con extensión en `JwtFilter`.
