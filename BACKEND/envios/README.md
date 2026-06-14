# SmartLogix – Microservicio de Envíos

Gestión de envíos y seguimiento de transportistas para la plataforma SmartLogix.

## Tecnologías

- Java 17 + Spring Boot 3.5
- Spring Data JPA + MySQL
- Spring Security + JWT
- JaCoCo (cobertura de pruebas)

## Requisitos

- Java 17+
- Maven 3.8+
- MySQL 8+

## Configuración

Base de datos: `db_smartlogix_envios` (se crea automáticamente).

Editar `src/main/resources/application.properties` si es necesario:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_smartlogix_envios
spring.datasource.username=root
spring.datasource.password=tu_password
```

## Ejecución

```bash
mvn spring-boot:run
```

El servicio queda disponible en `http://localhost:8085`.

## Endpoints principales

| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| POST | `/api/envios` | Crear envío y asignar transportista | ADMIN, OPERADOR |
| GET | `/api/envios` | Listar todos los envíos | ADMIN, OPERADOR |
| GET | `/api/envios/{id}` | Buscar envío por ID | ADMIN, OPERADOR, TRANSPORTISTA |
| GET | `/api/envios/transportista/{id}` | Envíos asignados al transportista | ADMIN, OPERADOR, TRANSPORTISTA |
| PUT | `/api/envios/{id}/estado?estado=X` | Actualizar estado del envío | TRANSPORTISTA |
| GET | `/api/envios/public/pedido/{pedidoId}` | Estado del envío (público, para seguimiento) | — |

### Estados del envío

`PENDIENTE` → `RECOGIDO` → `EN_TRANSITO` → `ENTREGADO` | `CANCELADO`

Al marcar un envío como `ENTREGADO`, el pedido asociado también se actualiza automáticamente.

## Pruebas

```bash
mvn test
```

Reporte JaCoCo: `target/site/jacoco/index.html`

## Documentación Swagger

Disponible en: `http://localhost:8085/swagger-ui.html`

## Autenticación

JWT Bearer Token (mismo secret que los demás microservicios).

## Patrones de diseño aplicados

- **Strategy**: `EnvioService` (interfaz) / `EnvioServiceImpl` (implementación).
- **Builder**: construcción de `Envio` y `EnvioResponse` con Lombok `@Builder`.
- **Repository**: acceso a datos abstraído mediante interfaces Spring Data JPA.
