Write-Host "🛑 Deteniendo servicios..." -ForegroundColor Cyan
docker compose down
Write-Host "✅ Todo detenido. Los datos (BD, documentos, índice) se conservan." -ForegroundColor Green