#!/usr/bin/env bash
set -e

echo "🛑 Deteniendo servicios..."
docker compose down

echo "✅ Todo detenido. Los datos (BD, documentos, índice) se conservan."