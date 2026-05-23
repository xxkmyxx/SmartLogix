# SmartLogix

Sistema de gestión logística desarrollado con arquitectura de microservicios. Permite administrar inventario, pedidos y usuarios de una empresa, con control de acceso por roles.

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
- JPA / Hibernate + H2
- Resilience4j (Circuit Breaker)
- Springdoc OpenAPI (Swagger)

**Frontend**
- React + Vite
- React Router
- Axios

---

## Arquitectura

```
FRONTEND (React)
     │
     ▼
   BFF :8084
  /    |    \
AUTH  INV  PED
:8082 :8081 :8083
```

El frontend se comunica exclusivamente con el BFF (Backend for Frontend), que actúa como proxy hacia los microservicios y agrega datos para el dashboard.

---

## Microservicios

| Servicio    | Puerto | Descripción                              |
|-------------|--------|------------------------------------------|
| auth        | 8082   | Autenticación, registro y gestión de usuarios |
| inventario  | 8081   | Productos, stock y bodegas               |
| pedidos     | 8083   | Gestión de pedidos y cambio de estados   |
| bff         | 8084   | Gateway hacia los microservicios         |
| frontend    | 5173   | Interfaz web (React + Vite)              |

---

## Roles

| Rol          | Permisos                                              |
|--------------|-------------------------------------------------------|
| ADMIN        | Acceso total: usuarios, inventario y pedidos          |
| OPERADOR     | Crear y gestionar pedidos, ver inventario             |
| TRANSPORTISTA| Ver y actualizar estado de pedidos                    |

---

## Cómo ejecutar

Requiere Java 17+ y Node 18+.

```bash
# 1. Auth
cd BACKEND/auth
mvn spring-boot:run

# 2. Inventario
cd BACKEND/inventario
mvn spring-boot:run

# 3. Pedidos
cd BACKEND/pedidos
mvn spring-boot:run

# 4. BFF
cd BACKEND/bff
mvn spring-boot:run

# 5. Frontend
cd FRONTEND
npm install
npm run dev
```

Luego abre `http://localhost:5173`.

Usuario administrador por defecto: `admin@smartlogix.cl`

---

## Documentación API

Cada microservicio expone Swagger UI:

- Auth: `http://localhost:8082/swagger-ui.html`
- Inventario: `http://localhost:8081/swagger-ui.html`
- Pedidos: `http://localhost:8083/swagger-ui.html`
- BFF: `http://localhost:8084/swagger-ui.html`
