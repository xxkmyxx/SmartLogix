# SmartLogix – Microservicio de Envíos

Gestión de envíos y seguimiento de transportistas para la plataforma SmartLogix.

## Requisitos

- Java 17
- Maven 3.8+
- MySQL 8.0+

## Configuración

Base de datos: `db_smartlogix_envios` (se crea automáticamente).

En `src/main/resources/application.properties` ajusta las credenciales de MySQL si es necesario.

Puerto: **8084**

## Ejecución

```bash
mvn spring-boot:run
```

## Pruebas

```bash
mvn test
```

Reporte JaCoCo: `target/site/jacoco/index.html`

## Endpoints principales

| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| POST | `/api/transportistas` | Registrar transportista | ADMIN |
| GET | `/api/transportistas` | Listar transportistas | ADMIN, OPERADOR |
| GET | `/api/transportistas/{id}` | Buscar transportista | ADMIN, OPERADOR, TRANSPORTISTA |
| DELETE | `/api/transportistas/{id}` | Eliminar transportista | ADMIN |
| POST | `/api/envios` | Crear envío | ADMIN, OPERADOR |
| GET | `/api/envios` | Listar envíos | ADMIN, OPERADOR |
| GET | `/api/envios/{id}` | Buscar envío por ID | ADMIN, OPERADOR, TRANSPORTISTA |
| GET | `/api/envios/pedido/{pedidoId}` | Envío por pedido | ADMIN, OPERADOR, TRANSPORTISTA |
| GET | `/api/envios/estado/{estado}` | Envíos por estado | ADMIN, OPERADOR |
| GET | `/api/envios/transportista/{id}` | Envíos por transportista | ADMIN, OPERADOR, TRANSPORTISTA |
| PUT | `/api/envios/{id}/estado?estado=X` | Actualizar estado | ADMIN, OPERADOR, TRANSPORTISTA |

### Estados del envío
`PENDIENTE` → `RECOGIDO` → `EN_TRANSITO` → `ENTREGADO` | `CANCELADO`

## Documentación Swagger

Disponible en: `http://localhost:8084/swagger-ui.html`

## Autenticación

JWT Bearer Token (mismo secret que los demás microservicios).
