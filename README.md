# Innovatech Solutions Platform

Plataforma interna de gestión de proyectos y tareas.
Arquitectura de microservicios con 5 servicios Spring Boot + API Gateway + React dashboard.

**Proyecto académico DuocUC — DSY1106-003V**
Equipo: Romina Baeza · Omar Felipe · Felipe Hernández

---

## Prerequisitos

- Java 17 (Eclipse Temurin): `java -version`
- Maven 3.9+: `mvn -version`
- Docker Desktop: `docker -version`
- Node.js 20+: `node -version`

---

## Setup inicial

### 1. Configurar variables de entorno
```bash
cp .env.example .env
# Editar .env si es necesario (el archivo ya tiene valores listos para desarrollo)
```

### 2. Levantar infraestructura (MySQL x5 + Redis)
```bash
docker-compose up -d mysql-auth mysql-users mysql-projects mysql-tasks mysql-reports redis
docker-compose ps   # todos deben aparecer como healthy
```

### 3. Compilar el monorepo
```bash
mvn clean install -DskipTests
```

### 4. Levantar servicios (cada uno en su terminal)
```bash
cd auth-service     && mvn spring-boot:run   # :8001
cd users-service    && mvn spring-boot:run   # :8002
cd gateway          && mvn spring-boot:run   # :8000
cd projects-service && mvn spring-boot:run   # :8003
cd tasks-service    && mvn spring-boot:run   # :8004
cd reports-service  && mvn spring-boot:run   # :8005
```

### 5. Levantar el frontend
```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

---

## Credenciales de acceso iniciales

| Campo    | Valor                  |
|----------|------------------------|
| Email    | admin@innovatech.cl    |
| Password | Admin2024!             |
| Rol      | ADMIN                  |

---

## Puertos del sistema

| Componente       | Puerto |
|------------------|--------|
| API Gateway      | :8000  |
| auth-service     | :8001  |
| users-service    | :8002  |
| projects-service | :8003  |
| tasks-service    | :8004  |
| reports-service  | :8005  |
| MySQL auth       | :3306  |
| MySQL users      | :3307  |
| MySQL projects   | :3308  |
| MySQL tasks      | :3309  |
| MySQL reports    | :3310  |
| Redis            | :6379  |
| Frontend (dev)   | :5173  |

---

## Swagger UI

Disponible en cada servicio una vez levantado:
- Auth: http://localhost:8001/swagger-ui.html
- Users: http://localhost:8002/swagger-ui.html
- Projects: http://localhost:8003/swagger-ui.html
- Tasks: http://localhost:8004/swagger-ui.html
- Reports: http://localhost:8005/swagger-ui.html

---

## Gateway

- Puerto del gateway: `http://localhost:8000`
- Rutas publicas: `/api/v1/auth/**`, `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- Rutas con JWT obligatorio: `/api/v1/projects/**`, `/api/v1/tasks/**`, `/api/v1/reports/**`
- Rutas solo ADMIN: `/api/v1/users/**`, `/api/v1/roles/**`, `/api/v1/permissions/**`
- Headers internos que agrega el gateway: `X-User-Id`, `X-User-Role`, `X-User-Email`
- Si el frontend quiere usar el gateway, debe apuntar sus llamadas a `http://localhost:8000/...`

---

## Comandos útiles

```bash
# Ver estado de contenedores
docker-compose ps

# Ver logs de un contenedor
docker-compose logs -f mysql-auth

# Detener toda la infraestructura
docker-compose down

# Detener y borrar volúmenes (BORRA LOS DATOS)
docker-compose down -v

# Ejecutar tests con reporte de cobertura
mvn verify
```
