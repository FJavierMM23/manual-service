# iniciar.ps1 — arranque con un solo comando en Windows
$ErrorActionPreference = "Stop"

Write-Host "🔍 Comprobando requisitos..." -ForegroundColor Cyan

# 1. Docker corriendo
try {
    docker info | Out-Null
} catch {
    Write-Host "❌ Docker no está corriendo. Arranca Docker Desktop e inténtalo de nuevo." -ForegroundColor Red
    exit 1
}

# 2. Ollama accesible (se queda en el host, no en Docker)
try {
    Invoke-WebRequest -Uri "http://localhost:11434/api/tags" -UseBasicParsing -TimeoutSec 5 | Out-Null
} catch {
    Write-Host "❌ Ollama no responde en localhost:11434." -ForegroundColor Red
    Write-Host "   Arráncalo (o comprueba que el servicio de Ollama está iniciado)." -ForegroundColor Yellow
    exit 1
}

# 3. La imagen de ai-service debe existir ya construida
$imagenExiste = docker image inspect ai-service:latest 2>$null
if (-not $imagenExiste) {
    Write-Host "⚠️  No encuentro la imagen ai-service:latest." -ForegroundColor Yellow
    Write-Host "   Constrúyela una vez con:" -ForegroundColor Yellow
    Write-Host "   cd ..\AI-service; docker build -t ai-service:latest ." -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Requisitos OK. Levantando servicios..." -ForegroundColor Green
docker compose up -d --build

Write-Host "⏳ Esperando a que todo esté listo..." -ForegroundColor Cyan
$listo = $false
while (-not $listo) {
    Start-Sleep -Seconds 2
    try {
        $respuesta = Invoke-RestMethod -Uri "http://localhost:8080/api/health" -TimeoutSec 3
        if ($respuesta.aiService -eq "ok") {
            $listo = $true
        } else {
            Write-Host "   ...todavía arrancando"
        }
    } catch {
        Write-Host "   ...todavía arrancando"
    }
}

Write-Host "🚀 Todo listo. Abriendo el navegador..." -ForegroundColor Green
Start-Process "http://localhost:8080"