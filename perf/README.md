# Pruebas de Rendimiento — Registraduría

Pruebas de carga y estrés sobre el endpoint `POST /register` de la app Spring Boot.
Herramientas: **k6** (ejecución) y **JMeter** (configuración `.jmx`).

---

## SLOs definidos

| Métrica | Objetivo |
|---------|----------|
| p95 latencia | ≤ 300 ms |
| p99 latencia | ≤ 800 ms |
| Tasa de errores | < 1% |
| Throughput mínimo | ≥ 100 req/s |

---

## Escenarios

| Escenario | VUs | Duración | Objetivo |
|-----------|-----|----------|---------|
| A — Baseline | 50 | 8 min | Línea base: medir p95/p99 estables |
| B — Carga | 0→200 | 15 min | Verificar comportamiento bajo demanda esperada |
| C — Estrés | 0→600 | 12 min | Detectar punto de quiebre y degradación |

---

## Estructura

```
perf/
├── scripts/
│   ├── register_voter_k6.js      # Script k6 con 3 escenarios
│   └── registraduria_load.jmx   # Plan JMeter con 3 Thread Groups
├── data/
│   └── voter.csv                 # 200 votantes de prueba
├── results/
│   ├── baseline.json             # Resultado k6 baseline
│   ├── load.json                 # Resultado k6 carga
│   └── stress.json               # Resultado k6 estrés
├── ci/
│   └── perf.yml                  # GitHub Actions pipeline
└── README.md                     # Este archivo
```

---

## Pre-requisitos

1. **Spring Boot corriendo** en `http://localhost:8080`
2. **k6 instalado**: `winget install grafana.k6`
3. Verificar: `k6 version`

### Levantar la app

```bash
# Desde la carpeta registraduria/
mvn -DskipTests spring-boot:run
```

Verificar que responde:
```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Ana\",\"id\":1,\"age\":30,\"gender\":\"FEMALE\",\"alive\":true}"
# Respuesta esperada: VALID
```

---

## Ejecución con k6

### Escenario A — Baseline
```bash
k6 run -e SCENARIO=baseline perf/scripts/register_voter_k6.js --out json=perf/results/baseline.json
```

### Escenario B — Carga
```bash
k6 run -e SCENARIO=load perf/scripts/register_voter_k6.js --out json=perf/results/load.json
```

### Escenario C — Estrés
```bash
k6 run -e SCENARIO=stress perf/scripts/register_voter_k6.js --out json=perf/results/stress.json
```

---

## Interpretación de resultados

Al terminar cada escenario, k6 muestra:

```
http_req_duration........: p(95)=XXXms   p(99)=XXXms
http_req_failed..........: X.XX%
iterations...............: XXXX
```

**Cómo leerlos:**
- `p(95)` → el 95% de las peticiones respondió en ese tiempo o menos
- `p(99)` → el 99% de las peticiones respondió en ese tiempo o menos
- `http_req_failed` → porcentaje de peticiones con error (HTTP != 200)
- Si `p(95) ≤ 300ms` y `failed < 1%` → ✅ cumple SLO

---

## Autor

Jonathan Muñoz Vargas — Universidad de La Sabana  
Curso: Testing y Validación de Software  
Docente: César Augusto Vega Fernández
