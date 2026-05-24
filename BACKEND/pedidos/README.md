# SmartLogix – Microservicio de Pedidos

Gestiona el ciclo de vida de los pedidos logísticos: creación, cambio de estado, cancelación y consulta, con verificación de stock en tiempo real mediante Circuit Breaker.

## Tecnologías

- Java 17 + Spring Boot 3.5
- Spring Data JPA + MySQL
- Resilience4j (Circuit Breaker)
- Spring Security + JWT
- JaCoCo (cobertura de pruebas)

## Requisitos

- Java 17+
- Maven 3.8+
- MySQL 8+
- Microservicio de Inventario en ejecución (puerto 8082)

## Configuración

Editar `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smartlogix_pedidos
spring.datasource.username=root
spring.datasource.password=tu_password

inventario.service.url=http://localhost:8082
```

## Ejecución

```bash
mvn spring-boot:run
```

El servicio queda disponible en `http://localhost:8083`.

## Endpoints principales

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/pedidos` | Crear pedido |
| GET | `/api/pedidos` | Listar todos los pedidos |
| GET | `/api/pedidos/{id}` | Buscar por ID |
| GET | `/api/pedidos/numero/{numero}` | Buscar por número |
| PATCH | `/api/pedidos/{id}/estado` | Cambiar estado |
| DELETE | `/api/pedidos/{id}` | Cancelar pedido |

## Estados del pedido

`PENDIENTE` → `CONFIRMADO` → `EN_PREPARACION` → `EN_TRANSITO` → `ENTREGADO`

Un pedido `ENTREGADO` o `CANCELADO` no puede cambiar de estado.

## Pruebas unitarias

```bash
mvn test
```

Reporte de cobertura: `target/site/jacoco/index.html`.

## Patrones de diseño aplicados

- **Circuit Breaker** (Resilience4j): si el microservicio de inventario no responde, el circuito se abre y retorna un valor de fallback (`-1`) para evitar cascada de fallos.
- **Builder**: `Pedido`, `DetallePedido` y `PedidoResponse` se construyen con `@Builder` de Lombok, garantizando objetos consistentes.
- **Strategy**: `PedidoService` / `PedidoServiceImpl` — desacopla la lógica de negocio del controlador REST.
