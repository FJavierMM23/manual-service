# Manual Service

Aplicación web (Spring Boot + Thymeleaf) para organizar apuntes de la carrera por asignatura y preguntarles en lenguaje natural, usando [`ai-service`](https://github.com/FJavierMM23/AI-service) como motor RAG. Sube tus documentos, se indexan automáticamente en segundo plano, y les preguntas desde un chat con respuestas citando la fuente exacta.

Construido con **Java 21 + Spring Boot 4 + PostgreSQL + Thymeleaf**, y `ai-service` (Python) como microservicio independiente para la parte de IA.

## Características

- **Gestión de asignaturas**: alta, listado con nº de documentos, borrado en cascada (con aviso de cuántos documentos se van a eliminar).
- **Subida de documentos**: indexación asíncrona en segundo plano (`@Async`) — la subida responde al instante y el estado (`PENDIENTE` → `INDEXADO`/`ERROR`) se actualiza solo, visible en la web con un badge de color.
- **Lectura de documentos**: botón para ver el fichero original directamente desde el navegador.
- **Chat con tus apuntes**: preguntas con historial de conversación (por sesión), filtrado opcional por asignatura, selector de modelo LLM (consultado en vivo a `ai-service`), y respuestas con markdown renderizado citando fuente y sección.
- **Temas de color**: 4 paletas seleccionables, persistidas en el navegador.
- **API REST** completa además de la interfaz web, con manejo de errores estandarizado (`ProblemDetail`, RFC 9457).

## Arquitectura

```
[navegador] ──► [manual-service (Spring Boot)] ──► [PostgreSQL]
                        │  sirve la web (Thymeleaf) + API REST
                        │
                        └──► [ai-service (Python/FastAPI)] ──► [Ollama + ChromaDB]
                                 RAG: indexación y preguntas
```

`manual-service` es dueño de los metadatos "de negocio" (asignaturas, títulos, temas) en PostgreSQL. `ai-service` es un motor RAG genérico que no sabe qué es una asignatura: solo guarda y filtra metadatos arbitrarios que `manual-service` le adjunta en cada documento. Los dos servicios son proyectos independientes, cada uno con su propio repositorio, y se comunican por HTTP.

**Flujo de subida de un documento:**
1. Se guarda en PostgreSQL con estado `PENDIENTE` y se responde `202 Accepted` al instante.
2. En segundo plano (`@Async`), el fichero se envía a `ai-service` con los metadatos de asignatura.
3. Al terminar, el estado pasa a `INDEXADO` (o `ERROR` si algo falla), visible en `/documentos` sin recargar nada especial — solo refrescando la página.

**Flujo de una pregunta:**
1. Se envía a `ai-service` (`POST /query`) junto con el filtro de asignatura (si se eligió uno) y el modelo LLM (si se eligió otro distinto al de por defecto).
2. La respuesta (con sus fuentes) se añade al historial de la conversación, guardado en la sesión HTTP.

## Requisitos

- **Docker** y **Docker Compose** (todo lo demás corre en contenedores).
- **[Ollama](https://ollama.com)** instalado en el host (no en Docker, para aprovechar tu GPU) con al menos un modelo de generación y `bge-m3` descargados — ver el [README de ai-service](https://github.com/FJavierMM23/AI-service) para la instalación y aceleración por GPU según tu sistema operativo.
- El repositorio de **[ai-service](https://github.com/FJavierMM23/AI-service)** clonado en una carpeta hermana a esta (`../AI-service`), porque su imagen Docker se construye desde ahí.

Para desarrollo sin Docker (editar código Java) además necesitas: **JDK 21** y **Maven** (o el `./mvnw` incluido).

## Arranque rápido (recomendado)

Con Ollama ya instalado y con los repos de `ai-service` y `manual-service` clonados como carpetas hermanas:

```
proyectos/
├── AI-service/
└── manual-service/
```

**Linux / macOS:**
```bash
cd manual-service
./iniciar.sh
```

**Windows (PowerShell):**
```powershell
cd manual-service
.\iniciar.ps1
```

El script comprueba que Docker y Ollama están accesibles, construye la imagen de `ai-service` si no existe, levanta los tres servicios (PostgreSQL, ai-service, manual-service) con `docker compose`, espera a que todo esté sano, y abre el navegador en `http://localhost:8080` automáticamente.

Si el script avisa de que falta la imagen de `ai-service`, constrúyela una vez:
```bash
cd ../AI-service
docker build -t ai-service:latest .
```
y vuelve a lanzar el script.

Para parar todo:
```bash
docker compose down
```
Los datos (PostgreSQL, el índice de ChromaDB, y los documentos subidos) persisten en volúmenes Docker entre arranques.

## Parada limpia
**Linux / macOS:**
./parar.sh
**Windows (PowerShell):**
.\parar.ps1

## Arranque manual (paso a paso, sin el script)

Útil para entender qué hace el script, o si prefieres controlar cada paso.

**1. Arranca Ollama** (si no arranca solo con el sistema):
```bash
ollama serve
```

**2. Construye la imagen de `ai-service`** (solo la primera vez, o tras actualizar su código):
```bash
cd ../AI-service
docker build -t ai-service:latest .
```

**3. Levanta todo con Docker Compose**, desde la carpeta de `manual-service`:
```bash
cd manual-service
docker compose up -d --build
```

**4. Comprueba que los tres servicios están sanos:**
```bash
docker compose ps
```
Los tres (`postgres`, `ai-service`, `manual-service`) deben aparecer como `Up (healthy)`.

**5. Verifica la conexión entre servicios:**
```bash
curl http://localhost:8080/api/health
```
Debe devolver `{"manualService":"ok","aiService":"ok"}`.

**6. Abre `http://localhost:8080`** en el navegador.

### Nota sobre Linux y `host.docker.internal`

`ai-service` corre en Docker pero necesita hablar con Ollama, que corre en tu host (fuera de Docker, para usar tu GPU). En Linux, además de la configuración ya incluida en el `docker-compose.yml`, puede hacer falta permitir el tráfico desde la red de contenedores hacia Ollama con `ufw`:
```bash
sudo ufw allow from 172.16.0.0/12 to any port 11434 proto tcp
```
En Windows/macOS con Docker Desktop no hace falta ningún ajuste adicional.

## Desarrollo local (sin Docker, para tocar código)

Si vas a editar el código Java y quieres ciclos de recarga rápidos, es más cómodo correr `manual-service` directamente con Maven en vez de reconstruir la imagen en cada cambio.

**1. Solo PostgreSQL en Docker:**
```bash
docker compose up -d postgres
```

**2. `ai-service` corriendo aparte** (con Ollama arriba), desde su repo:
```bash
cd ../AI-service
uvicorn ai_service.api.app:app --reload
```

**3. `manual-service` desde el IDE o terminal:**
```bash
./mvnw spring-boot:run
```

Con las variables de entorno por defecto (`localhost` para todo), esto conecta automáticamente con el PostgreSQL y el `ai-service` que acabas de levantar, sin tocar ningún fichero de configuración — los mismos `application.yml` sirven tanto en local como en Docker gracias a los placeholders `${VARIABLE:default}`.

## Configuración

Variables de entorno (fichero `.env` en la raíz del proyecto, leído por Docker Compose; copia `.env.example` como punto de partida):

| Variable | Por defecto | Descripción |
|----------|-------------|-------------|
| `DB_PASSWORD` | `cambiame` | Contraseña de PostgreSQL. **Cámbiala** antes de un uso real. |
| `LLM_MODEL` | `gemma4:e4b-it-q4_K_M` | Modelo de generación por defecto que usa `ai-service` (ver su README para alternativas). |

> **Sobre `DB_PASSWORD` y volúmenes existentes:** PostgreSQL solo aplica esta variable la **primera vez** que inicializa un volumen vacío. Si cambias la contraseña con el volumen `postgres_data` ya creado, PostgreSQL seguirá usando la contraseña antigua y `manual-service` fallará al conectar (`password authentication failed`). Para aplicar una contraseña nueva: `docker compose down && docker volume rm manual-service_postgres_data` (esto borra los datos) y vuelve a arrancar.

Otras variables (usadas internamente por el compose, normalmente no hace falta tocarlas): `DB_URL`, `DB_USER`, `AI_SERVICE_URL`, `DOCUMENTO_STORAGE_PATH`.

## Estructura del proyecto

```
src/main/java/es/urjc/manualservice/
├── asignatura/        # Entidad, repositorio, servicio, controller REST y DTOs
├── documento/          # Igual, + IndexacionService (@Async) y almacenamiento en disco
├── consulta/           # Cliente de preguntas hacia ai-service
├── aiservice/           # AiServiceClient (RestClient hacia ai-service), DTOs del contrato
├── web/                 # Controllers de VISTAS (Thymeleaf): asignatura, documento, consulta
├── shared/              # GlobalExceptionHandler, HealthController
└── config/              # AsyncConfig, AiServiceConfig

src/main/resources/
├── db/migration/         # Migraciones Flyway (V1, V2, V3...)
├── templates/             # Vistas Thymeleaf (fragments/, asignaturas/, documentos/, preguntar.html)
├── static/css/            # estilo.css + temas.css (paletas de color)
└── application.yml

src/test/                  # Tests con Testcontainers (PostgreSQL real) + Mockito
```

### Decisiones de diseño

- **`ddl-auto: validate`**: el esquema de la base de datos lo define Flyway (SQL versionado), nunca Hibernate. Si una entidad y una migración no coinciden, la aplicación no arranca — se detecta el desajuste al instante en vez de en producción.
- **`@Async` para la indexación**: subir un documento no bloquea al usuario esperando a que `ai-service` termine de procesarlo (puede tardar varios segundos). El estado se consulta o se ve reflejado solo al refrescar la lista.
- **`RestClient` sobre Apache HttpClient5** (no el `HttpClient` del JDK) para hablar con `ai-service`: el cliente del JDK intenta negociar HTTP/2 por defecto, lo que provocaba fallos silenciosos contra `uvicorn`.
- **Filtrado por metadatos, no por relación en base de datos**: `manual-service` envía la asignatura como metadato al indexar, y `ai-service` filtra nativamente en ChromaDB (`where`). Esto mantiene a `ai-service` genérico y desacoplado — no necesita saber qué es una "asignatura".

## API REST

Además de la interfaz web, todos los recursos son accesibles por API:

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET`/`POST` | `/api/asignaturas` | Listar / crear asignaturas |
| `GET`/`PUT`/`DELETE` | `/api/asignaturas/{id}` | Obtener / actualizar / eliminar (en cascada) |
| `POST` | `/api/documentos` | Subir documento (multipart: `metadata` + `file`) |
| `GET` | `/api/documentos/{id}` | Obtener un documento |
| `GET` | `/api/documentos/{id}/archivo` | Descargar/ver el fichero original |
| `DELETE` | `/api/documentos/{id}` | Eliminar (en PostgreSQL, ai-service y disco) |
| `GET` | `/api/asignaturas/{id}/documentos` | Documentos de una asignatura |
| `GET` | `/api/health` | Estado combinado de manual-service y ai-service |

Los errores siguen el estándar [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) (`ProblemDetail`): `404` con `detail` descriptivo si no existe el recurso, `409` en conflictos (siglas duplicadas), `400` con los mensajes de validación campo a campo.

## Tests

```bash
./mvnw test
```

Usa [Testcontainers](https://testcontainers.com/) para levantar un PostgreSQL real en Docker durante los tests (no una base de datos simulada), y Mockito para los tests unitarios de lógica de negocio aislada. Docker debe estar corriendo para ejecutar la suite completa.

## Licencia

Proyecto personal de aprendizaje - Uso libre.