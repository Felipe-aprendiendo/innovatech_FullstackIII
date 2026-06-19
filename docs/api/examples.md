# API Request and Response Examples

This file provides concrete examples to test communication between services and to support the API REST deliverable.

## 1. Auth - Login

Endpoint:

```text
POST http://localhost:8001/api/v1/auth/login
```

Request:

```json
{
  "email": "admin@innovatech.cl",
  "password": "Admin2024!"
}
```

Expected response:

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": 1,
  "email": "admin@innovatech.cl",
  "permissions": [
    "ROLE_ADMIN"
  ]
}
```

## 2. Users - Create user

Endpoint:

```text
POST http://localhost:8002/api/v1/users
```

Request:

```json
{
  "name": "Codex",
  "lastName": "Verifier",
  "email": "codex.verifier@innovatech.cl",
  "password": "Codex2026!",
  "roleIds": [1]
}
```

Expected response:

```json
{
  "id": 3,
  "name": "Codex",
  "lastName": "Verifier",
  "email": "codex.verifier@innovatech.cl",
  "enabled": true,
  "roles": [
    {
      "id": 1,
      "name": "ADMIN"
    }
  ]
}
```

## 3. Projects - Create project

Endpoint:

```text
POST http://localhost:8003/api/v1/projects
```

Request:

```json
{
  "nombre": "Portal Interno",
  "descripcion": "Proyecto de soporte operativo",
  "prioridad": "MEDIA",
  "fechaInicio": "2026-06-19",
  "fechaFin": "2026-07-30",
  "responsableId": 1,
  "miembroIds": [1, 2]
}
```

Expected response:

```json
{
  "id": 1,
  "nombre": "Portal Interno",
  "descripcion": "Proyecto de soporte operativo",
  "estado": "PLANIFICADO",
  "prioridad": "MEDIA",
  "fechaInicio": "2026-06-19",
  "fechaFin": "2026-07-30",
  "responsableId": 1,
  "miembroIds": [1, 2],
  "avance": {
    "totalTareas": 0,
    "tareasPendientes": 0,
    "tareasEnProgreso": 0,
    "tareasCompletadas": 0,
    "tareasCanceladas": 0,
    "porcentajeAvance": 0
  }
}
```

## 4. Tasks - Create task

Endpoint:

```text
POST http://localhost:8004/api/v1/tasks
```

Required headers:

```text
X-User-Id: 1
X-User-Role: ADMIN
```

Request:

```json
{
  "titulo": "Crear dashboard inicial",
  "descripcion": "Implementar vista inicial del modulo reportes",
  "projectId": 1,
  "responsableId": 2,
  "prioridad": "ALTA"
}
```

Expected response:

```json
{
  "success": true,
  "message": "Tarea creada exitosamente",
  "data": {
    "id": 1,
    "titulo": "Crear dashboard inicial",
    "descripcion": "Implementar vista inicial del modulo reportes",
    "estado": "PENDIENTE",
    "prioridad": "ALTA",
    "projectId": 1,
    "responsableId": 2,
    "createdBy": 1
  }
}
```

## 5. Tasks - List project tasks

Endpoint:

```text
GET http://localhost:8004/api/v1/tasks/project/1
```

Required headers:

```text
X-User-Id: 1
X-User-Role: ADMIN
```

Expected response:

```json
{
  "success": true,
  "message": "Tareas del proyecto obtenidas",
  "data": [
    {
      "id": 1,
      "titulo": "Crear dashboard inicial",
      "estado": "PENDIENTE",
      "prioridad": "ALTA",
      "projectId": 1,
      "responsableId": 2
    }
  ]
}
```

## 6. Reports - Dashboard

Endpoint:

```text
GET http://localhost:8005/api/v1/reports/dashboard
```

Required headers:

```text
X-User-Id: 1
X-User-Role: ADMIN
```

Expected response:

```json
{
  "success": true,
  "message": "Dashboard obtenido",
  "data": {
    "totalProyectos": 1,
    "totalTareas": 1,
    "tareasPendientes": 1,
    "tareasEnProgreso": 0,
    "tareasCompletadas": 0,
    "proyectos": [
      {
        "projectId": 1,
        "nombreProyecto": "Portal Interno"
      }
    ]
  }
}
```

## 7. Gateway-mediated request

The frontend should normally consume the backend through the gateway:

```text
GET http://localhost:8000/api/v1/projects
Authorization: Bearer <accessToken>
```

Gateway behavior:

- Validates JWT
- Blocks unauthorized requests
- Adds internal headers:
  - `X-User-Id`
  - `X-User-Role`
  - `X-User-Email`
- Forwards the request to the target microservice
