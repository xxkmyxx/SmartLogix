# SmartLogix – Backend For Frontend (BFF)

Actúa como punto único de entrada para el frontend. Agrega datos de los microservicios de auth, inventario y pedidos, y expone una API simplificada adaptada a las necesidades del cliente web.

## Tecnologías

- Java 17 + Spring Boot 3.5
- Spring Security + JWT
- Resilience4j (Circuit Breaker entre BFF y microservicios)
- RestTemplate (comunicación HTTP interna)
- SpringDoc OpenAPI (documentación Swagger)

## Requisitos

- Java 17+
- Maven 3.8+
- Microservicios en ejecución:
  - auth en `http://localhost:8081`
  - inventario en `http://localhost:8082`
  - pedidos en `http://localhost:8083`

## Configuración

Editar `src/main/resources/application.properties`:

```properties
server.port=8080

auth.service.url=http://localhost:8081
inventario.service.url=http://localhost:8082
pedidos.service.url=http://localhost:8083

jwt.secret=smartlogix-secret-key
jwt.expiration=86400000
jwt.prefix=Bearer
jwt.header=Authorization
```

## Ejecución

```bash
mvn spring-boot:run
```

El BFF queda disponible en `http://localhost:8080`.

Documentación Swagger: `http://localhost:8080/swagger-ui.html`

## Endpoints principales (proxy)

| Método | Ruta BFF | Destino |
|--------|----------|---------|
| POST | `/api/auth/**` | Microservicio auth |
| GET/POST | `/api/inventario/**` | Microservicio inventario |
| GET/POST | `/api/pedidos/**` | Microservicio pedidos |
| GET | `/api/dashboard` | Datos agregados (auth + inventario + pedidos) |

## Patrones de diseño aplicados

- **Backend For Frontend (BFF)**: patrón arquitectónico que adapta la API de los microservicios a las necesidades específicas del cliente web, evitando que el frontend llame directamente a múltiples servicios.
- **Proxy**: los controladores del BFF redirigen las solicitudes hacia los microservicios correspondientes, añadiendo autenticación y manejo de errores.
- **Circuit Breaker** (Resilience4j): si un microservicio destino no responde, el BFF retorna una respuesta de fallback sin propagar el error al usuario.
