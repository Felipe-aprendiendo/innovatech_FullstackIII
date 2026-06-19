$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$outputDir = Join-Path $PSScriptRoot "openapi"

if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$services = @(
    @{
        Name = "auth-service"
        Url = "http://localhost:8001/api-docs"
        Output = "auth-service-openapi.json"
    },
    @{
        Name = "users-service"
        Url = "http://localhost:8002/api-docs"
        Output = "users-service-openapi.json"
    },
    @{
        Name = "projects-service"
        Url = "http://localhost:8003/api-docs"
        Output = "projects-service-openapi.json"
    },
    @{
        Name = "tasks-service"
        Url = "http://localhost:8004/api-docs"
        Output = "tasks-service-openapi.json"
    },
    @{
        Name = "reports-service"
        Url = "http://localhost:8005/api-docs"
        Output = "reports-service-openapi.json"
    }
)

Write-Output "Exporting OpenAPI files to: $outputDir"

foreach ($service in $services) {
    $target = Join-Path $outputDir $service.Output

    try {
        Invoke-WebRequest -UseBasicParsing -Uri $service.Url -OutFile $target
        Write-Output "[OK] $($service.Name) -> $target"
    } catch {
        Write-Warning "[SKIP] $($service.Name) could not be exported from $($service.Url)"
    }
}

Write-Output "Done."
