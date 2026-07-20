#!/usr/bin/env bash
set -e

echo "🔍 Comprobando requisitos..."

# 1. Docker corriendo
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker no está corriendo. Arráncalo e inténtalo de nuevo."
    exit 1
fi

# 2. Ollama accesible (se queda en el host, no en Docker)
if ! curl -sf http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo "❌ Ollama no responde en localhost:11434."
    echo "   Arráncalo con: ollama serve"
    exit 1
fi

# 3. La imagen de ai-service debe existir ya construida
if ! docker image inspect ai-service:latest > /dev/null 2>&1; then
    echo "⚠️  No encuentro la imagen ai-service:latest."
    echo "   Constrúyela una vez con:"
    echo "   cd ../AI-service && docker build -t ai-service:latest ."
    exit 1
fi

echo "✅ Requisitos OK. Levantando servicios..."
docker compose up -d --build

echo "⏳ Esperando a que todo esté listo..."
until curl -sf http://localhost:8080/api/health | grep -q '"aiService":"ok"'; do
    sleep 2
    echo "   ...todavía arrancando"
done

echo "🚀 Todo listo. Abriendo el navegador..."
xdg-open http://localhost:8080 2>/dev/null || echo "Abre manualmente: http://localhost:8080"