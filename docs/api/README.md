# API REST Documentation

This folder centralizes the deliverables requested for the API REST section of the project:

- Swagger/OpenAPI files exported from each microservice
- Practical request and response examples
- A script to export the OpenAPI JSON files once the services are running

## Services with Swagger/OpenAPI

| Service | Base URL | Swagger UI | OpenAPI JSON |
|---------|----------|------------|--------------|
| auth-service | `http://localhost:8001/api/v1` | `http://localhost:8001/swagger-ui.html` | `http://localhost:8001/api-docs` |
| users-service | `http://localhost:8002` | `http://localhost:8002/swagger-ui.html` | `http://localhost:8002/api-docs` |
| projects-service | `http://localhost:8003` | `http://localhost:8003/swagger-ui.html` | `http://localhost:8003/api-docs` |
| tasks-service | `http://localhost:8004` | `http://localhost:8004/swagger-ui.html` | `http://localhost:8004/api-docs` |
| reports-service | `http://localhost:8005` | `http://localhost:8005/swagger-ui.html` | `http://localhost:8005/api-docs` |

## How to export the Swagger files

1. Start the microservices.
2. Open a PowerShell terminal at the repository root.
3. Run:

```powershell
.\docs\api\export-openapi.ps1
```

4. The generated files will be saved in:

```text
docs/api/openapi/
```

Expected output files:

- `auth-service-openapi.json`
- `users-service-openapi.json`
- `projects-service-openapi.json`
- `tasks-service-openapi.json`
- `reports-service-openapi.json`

## Suggested submission package

For the API REST requirement, the recommended bundle is:

1. Exported Swagger/OpenAPI JSON files from each microservice.
2. The practical examples in [examples.md](/c:/Users/USUARIO/Desktop/Nueva%20carpeta/innovatech_FullstackIII/docs/api/examples.md).
3. Optional: a Postman collection built from the same routes for live demonstrations.

## Notes

- The gateway does not expose its own business OpenAPI contract. It acts as a proxy and security layer for the backend services.
- If a service is not running, the export script will report it and continue with the remaining services.
