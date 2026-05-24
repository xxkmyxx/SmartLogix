# SmartLogix – Microservicio de Inventario

Gestiona el stock de productos por bodega, registra movimientos de entrada/salida y permite sincronización entre bodegas.

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

Editar `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smartlogix_inventario
spring.datasource.username=root
spring.datasource.password=tu_password
```

## Ejecución

```bash
mvn spring-boot:run
```

El servicio queda disponible en `http://localhost:8082`.

## Endpoints principales

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/inventario/bodega/{id}` | Stock por bodega |
| GET | `/api/inventario/producto/{id}` | Stock por producto |
| POST | `/api/inventario/sincronizar` | Transferencia entre bodegas |
| GET | `/api/bodegas` | Listar bodegas |

## Pruebas unitarias

```bash
mvn test
```

Reporte de cobertura: `target/site/jacoco/index.html`.

## Patrones de diseño aplicados

- **Factory Method**: `ProductoFactory.crearMovimiento(...)` — centraliza la creación de registros `MovimientoStock`, garantizando coherencia en los campos obligatorios (tipo, fecha, usuario).
- **Strategy**: `InventarioService` / `InventarioServiceImpl` — desacopla la lógica de negocio del controlador.
- **Repository** (patrón arquitectónico): acceso a datos abstraído mediante interfaces Spring Data JPA.
