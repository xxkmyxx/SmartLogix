# SmartLogix

Sistema de gestión logística desarrollado con arquitectura de microservicios. Permite administrar inventario, pedidos, envíos y usuarios de una empresa, con control de acceso por roles.

---

## Integrantes

- Camila Pino
- Gonzalo Navarrete
- Carla Prado

---

## Tecnologías

**Backend**
- Java 17 + Spring Boot 3
- Spring Security + JWT
- JPA / Hibernate + MySQL
- H2 (solo pruebas)
- Resilience4j (Circuit Breaker)
- Springdoc OpenAPI (Swagger)

**Frontend**
- React + Vite
- React Router
- Axios

---

## Arquitectura

```
FRONTEND (React :5173)
         │
         ▼
      BFF :8084
   /   |   |   \
AUTH  INV  PED  ENV
:8081 :8082 :8083 :8085
```

El frontend se comunica exclusivamente con el BFF (Backend for Frontend), que actúa como proxy hacia los microservicios y aplica Circuit Breaker (Resilience4j) en cada ruta.

---

## Microservicios

| Servicio    | Puerto | Base de datos               | Descripción                                    |
|-------------|--------|-----------------------------|------------------------------------------------|
| auth        | 8081   | db_smartlogix_auth          | Autenticación, registro y gestión de usuarios  |
| inventario  | 8082   | db_smartlogix_inventario    | Productos, stock y bodegas                     |
| pedidos     | 8083   | db_smartlogix_pedidos       | Gestión de pedidos y cambio de estados         |
| envios      | 8085   | db_smartlogix_envios        | Gestión de envíos y seguimiento de transportistas |
| bff         | 8084   | —                           | Gateway y orquestador hacia los microservicios |
| frontend    | 5173   | —                           | Interfaz web (React + Vite)                    |

---

## Roles

| Rol           | Permisos                                                                  |
|---------------|---------------------------------------------------------------------------|
| ADMIN         | Acceso total: usuarios, inventario, pedidos y envíos                      |
| OPERADOR      | Crear y gestionar pedidos y envíos, ver inventario                        |
| TRANSPORTISTA | Ver pedidos y envíos asignados, actualizar estado de sus envíos           |
| CLIENTE       | Acceso exclusivo al Portal B2B: catálogo, comprar, ver sus pedidos        |

---

## Cómo ejecutar

Requiere Java 17+, Maven 3.8+, MySQL 8.0+ y Node 18+.

```bash
# 1. Inventario
cd BACKEND/inventario
mvn spring-boot:run

# 2. Auth
cd BACKEND/auth
mvn spring-boot:run

# 3. Pedidos
cd BACKEND/pedidos
mvn spring-boot:run

# 4. Envíos
cd BACKEND/envios
mvn spring-boot:run

# 5. BFF (último)
cd BACKEND/bff
mvn spring-boot:run

# 6. Frontend
cd FRONTEND
npm install
npm run dev
```

Luego abre `http://localhost:5173`.

Usuario administrador por defecto: `admin@smartlogix.cl` / `admin123`

Las bases de datos se crean automáticamente al levantar cada microservicio.

---

## Portal B2B

Los clientes acceden por `http://localhost:5173/portal` con su cuenta de rol `CLIENTE`.

Funcionalidades disponibles:
- **Catálogo**: ver productos con stock disponible y realizar pedidos
- **Mis pedidos**: historial de pedidos con estado en tiempo real
- **Seguimiento**: consulta pública de estado por número de pedido en `http://localhost:5173/seguimiento`

---

## Pruebas unitarias

```bash
# Ejecutar tests y generar reporte JaCoCo en cada microservicio
mvn test

# Reporte en: target/site/jacoco/index.html
```

---

## Documentación API (Swagger)

| Microservicio | URL                                    |
|---------------|----------------------------------------|
| Auth          | http://localhost:8081/swagger-ui.html  |
| Inventario    | http://localhost:8082/swagger-ui.html  |
| Pedidos       | http://localhost:8083/swagger-ui.html  |
| Envíos        | http://localhost:8085/swagger-ui.html  |
| BFF           | http://localhost:8084/swagger-ui.html  |
