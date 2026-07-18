# inicializar el proyecto:
## Terminal 1 — ai-service (uvicorn):
```bash
~/Documentos/AI-service # Ollama suele arrancar solo con el sistema. Si no, en otra terminal: ollama serve
source .venv/bin/activate          # fish: source .venv/bin/activate.fish
uvicorn ai_service.api.app:app --reload
```
Espera a ver ✓ Ollama accesible. ai-service listo. Si sale un error de que no encuentra Ollama, es que el servicio de Ollama no arrancó: ejecuta ollama serve en una terminal aparte (o comprueba con ollama list).
## Terminal 2 — Postgres + manual-service:
```bash
cd ~/Documentos/manual-service
docker compose up -d postgres      # levanta la BD en segundo plano
docker compose ps                  # confirma que pone "healthy"
./mvnw spring-boot:run
```
El docker compose up -d postgres es el que no puedes olvidar — al apagar el PC el contenedor se para, y sin Postgres manual-service no arranca. Tus datos siguen ahí (el volumen postgres_data persiste en disco), solo hay que volver a encender el contenedor. Espera el Started ManualServiceApplication.
## Terminal 3 — pruebas de manual-service (curl, git, etc.):
```bash
cd ~/Documentos/manual-service
```
Esta la dejas libre para trabajar.
Comprobación de que todo está en pie, desde la Terminal 3:
```bash
curl localhost:8080/api/health
```