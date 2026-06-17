# Gestión de Defectos — Pruebas de Rendimiento

## Defecto 01 — Degradación de latencia bajo carga alta

| Campo | Detalle |
|-------|---------|
| **ID** | PERF-01 |
| **Escenario** | C — Estrés (200→600 VUs) |
| **Métrica afectada** | p95 latencia |
| **Valor observado** | p95 ≈ 850ms @ 400+ VUs |
| **SLO esperado** | p95 ≤ 300ms |
| **Causa probable** | H2 in-memory procesa registros de forma secuencial; a partir de ~300 VUs concurrentes los hilos de Spring Boot compiten por el mismo pool de conexiones JDBC |
| **Evidencia** | Resultado `stress.json` — la curva de latencia sube linealmente después de los 300 VUs |
| **Estado** | Abierto |
| **Mejora propuesta** | Configurar HikariCP con `maximum-pool-size=20` en `application.properties`; en producción usar PostgreSQL con pool dedicado |

---

## Defecto 02 — Incremento de tasa de error en estrés extremo

| Campo | Detalle |
|-------|---------|
| **ID** | PERF-02 |
| **Escenario** | C — Estrés (500–600 VUs) |
| **Métrica afectada** | Tasa de errores HTTP |
| **Valor observado** | ~3.2% de errores 500 a partir de 500 VUs |
| **SLO esperado** | < 1% |
| **Causa probable** | Agotamiento del thread pool de Spring Boot (por defecto 200 hilos en Tomcat embebido); las peticiones en cola superan el timeout y devuelven 503/500 |
| **Evidencia** | Resultado `stress.json` — `http_req_failed` sube de 0% a 3.2% entre los 400 y 600 VUs |
| **Estado** | Abierto |
| **Mejora propuesta** | Ajustar `server.tomcat.threads.max=400` y `server.tomcat.accept-count=200`; evaluar modelo reactivo (Spring WebFlux) para alta concurrencia |
