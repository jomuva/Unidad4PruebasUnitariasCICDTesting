// =============================================================
// register_voter_k6.js
// Pruebas de carga para POST /register — Registraduría
// Escenarios: baseline | load | stress
// Uso:
//   k6 run -e SCENARIO=baseline register_voter_k6.js
//   k6 run -e SCENARIO=load    register_voter_k6.js
//   k6 run -e SCENARIO=stress  register_voter_k6.js
// =============================================================

import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend, Rate } from "k6/metrics";
import { SharedArray } from "k6/data";
import papaparse from "https://jslib.k6.io/papaparse/5.1.1/index.js";

// ── Métricas personalizadas ────────────────────────────────────
const errorCount   = new Counter("custom_errors");
const responseTime = new Trend("custom_response_ms", true);
const successRate  = new Rate("custom_success_rate");

// ── Datos de prueba desde CSV ──────────────────────────────────
const voters = new SharedArray("voters", function () {
  const raw = open("../data/voter.csv");
  return papaparse.parse(raw, { header: true, skipEmptyLines: true }).data;
});

// ── SLOs ───────────────────────────────────────────────────────
const SLO_P95_MS  = 300;
const SLO_P99_MS  = 800;
const SLO_ERR_PCT = 0.01;  // 1%

// ── Configuración de escenarios ────────────────────────────────
const SCENARIO = __ENV.SCENARIO || "baseline";
const BASE_URL  = __ENV.BASE_URL  || "http://localhost:8080";

const scenarios = {

  // ── A: BASELINE — calentamiento + medición estable 50 VUs ───
  baseline: {
    executor: "ramping-vus",
    startVUs: 0,
    stages: [
      { duration: "2m",  target: 10 },   // warmup
      { duration: "5m",  target: 50 },   // medición estable
      { duration: "1m",  target: 0  },   // cooldown
    ],
    gracefulRampDown: "30s",
  },

  // ── B: CARGA — rampa 0→200 VUs, sostener 10 min ─────────────
  load: {
    executor: "ramping-vus",
    startVUs: 0,
    stages: [
      { duration: "3m",  target: 50  },  // rampa baja
      { duration: "5m",  target: 100 },  // rampa media
      { duration: "5m",  target: 200 },  // pico de carga
      { duration: "2m",  target: 0   },  // cooldown
    ],
    gracefulRampDown: "30s",
  },

  // ── C: ESTRÉS — detectar punto de quiebre ───────────────────
  stress: {
    executor: "ramping-vus",
    startVUs: 0,
    stages: [
      { duration: "2m",  target: 100 },  // base
      { duration: "3m",  target: 200 },  // carga
      { duration: "3m",  target: 400 },  // estrés
      { duration: "2m",  target: 600 },  // saturación
      { duration: "2m",  target: 0   },  // cooldown
    ],
    gracefulRampDown: "30s",
  },
};

// ── Opciones k6 ────────────────────────────────────────────────
export const options = {
  scenarios: {
    [SCENARIO]: scenarios[SCENARIO],
  },
  thresholds: {
    "http_req_duration":            [`p(95)<${SLO_P95_MS}`, `p(99)<${SLO_P99_MS}`],
    "http_req_failed":              [`rate<${SLO_ERR_PCT}`],
    "custom_success_rate":          ["rate>0.99"],
    "custom_response_ms":           [`p(95)<${SLO_P95_MS}`],
  },
};

// ── Función principal ejecutada por cada VU ────────────────────
export default function () {
  // Seleccionar un votante del CSV de forma circular
  const voter = voters[(__VU * __ITER + __ITER) % voters.length];

  const payload = JSON.stringify({
    name:   voter.name,
    id:     parseInt(voter.id),
    age:    parseInt(voter.age),
    gender: voter.gender,
    alive:  voter.alive === "true",
  });

  const params = {
    headers: { "Content-Type": "application/json" },
    timeout: "3s",
  };

  const res = http.post(`${BASE_URL}/register`, payload, params);

  // ── Validaciones (checks) ──────────────────────────────────
  const ok = check(res, {
    "status 200":           (r) => r.status === 200,
    "responde VALID o DUPLICATED": (r) =>
      r.body === "VALID" || r.body === "DUPLICATED",
    "p95 < 300ms":          (r) => r.timings.duration < SLO_P95_MS,
  });

  // ── Métricas personalizadas ────────────────────────────────
  responseTime.add(res.timings.duration);
  successRate.add(ok);
  if (!ok || res.status !== 200) {
    errorCount.add(1);
  }

  sleep(0.5);
}

// ── Resumen al finalizar ───────────────────────────────────────
export function handleSummary(data) {
  const p95  = data.metrics.http_req_duration?.values?.["p(95)"] || 0;
  const p99  = data.metrics.http_req_duration?.values?.["p(99)"] || 0;
  const err  = (data.metrics.http_req_failed?.values?.rate || 0) * 100;
  const rps  = data.metrics.http_reqs?.values?.rate || 0;
  const reqs = data.metrics.http_reqs?.values?.count || 0;

  const status = p95 <= SLO_P95_MS && err <= 1 ? "✅ CUMPLE SLO" : "❌ INCUMPLE SLO";

  return {
    stdout: `
========================================================
  RESUMEN — Escenario: ${SCENARIO.toUpperCase()}
========================================================
  Total requests : ${reqs}
  Throughput     : ${rps.toFixed(1)} req/s
  p95 latencia   : ${p95.toFixed(1)} ms   (SLO: ≤${SLO_P95_MS}ms)
  p99 latencia   : ${p99.toFixed(1)} ms   (SLO: ≤${SLO_P99_MS}ms)
  Tasa de error  : ${err.toFixed(2)}%     (SLO: <1%)
  Resultado      : ${status}
========================================================\n`,
  };
}
