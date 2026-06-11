# Registraduría — Sistema de Registro de Votantes

Sistema de validación y registro de votantes desarrollado con **Spring Boot 3** y **Java 17**,
con pipeline **CI/CD** completo usando **GitHub Actions** (CI) y **Jenkins** (CD).

---

## Tecnologías

| Capa | Tecnología |
|------|-----------|
| Backend | Java 17 + Spring Boot 3 |
| Base de datos (pruebas) | H2 in-memory |
| Pruebas | JUnit 5 + Mockito |
| Cobertura | JaCoCo |
| Contenedor | Docker (multi-stage build) |
| CI | GitHub Actions |
| CD | Jenkins |
| Registro de imágenes | DockerHub |

---

## Flujo CI/CD

```
Developer
    |
    | git push / pull request
    v
+----------------------------------+
|  PIPELINE CI — GitHub Actions    |
|  1. Checkout del código          |
|  2. Configurar Java 17 + Maven   |
|  3. mvn clean verify             |
|     (compilar + pruebas unitarias|
|      + pruebas de integración)   |
|  4. Generar reporte JaCoCo       |
+----------------------------------+
    |
    | CI verde → activar CD manualmente
    v
+----------------------------------+
|  PIPELINE CD — Jenkins           |
|  1. Clonar repositorio           |
|  2. docker build (imagen Docker) |
|  3. docker push → DockerHub      |
|  4. Verificar imagen publicada   |
+----------------------------------+
    |
    | Imagen lista para despliegue
    v
+----------------------------------+
|  DockerHub Registry              |
|  usuario/registraduria:latest    |
+----------------------------------+
```

---

## Pipeline CI — GitHub Actions

El pipeline CI se activa automáticamente ante cada **push** o **pull request**
hacia las ramas `main` o `master`.

**Archivo:** `.github/workflows/ci.yml`

### Etapas del CI

| # | Etapa | Descripción |
|---|-------|-------------|
| 1 | Checkout | Descarga el código fuente del repositorio |
| 2 | Configurar Java 17 | Instala el JDK con caché de dependencias Maven |
| 3 | mvn clean verify | Compila, ejecuta todas las pruebas y genera el .jar |
| 4 | Reporte JaCoCo | Genera métricas de cobertura de código |

### Ver ejecuciones del CI

Ir a la pestaña **Actions** del repositorio en GitHub.

---

## Pipeline CD — Jenkins

El pipeline CD define los stages de construcción y publicación de la imagen Docker.

**Archivo:** `Jenkinsfile`

### Stages del CD

| # | Stage | Descripción |
|---|-------|-------------|
| 1 | Clonar repositorio | `git clone` del repositorio desde GitHub |
| 2 | Construir imagen Docker | `docker build` usando el `Dockerfile` de la raíz |
| 3 | Publicar en DockerHub | Push de la imagen con tag de build y `latest` |
| 4 | Verificar imagen | `docker pull` de verificación post-publicación |

---

## Dockerfile — Construcción de la imagen

El `Dockerfile` usa **multi-stage build** para generar una imagen liviana y segura:

- **Etapa 1 (build):** Compila el proyecto con Maven (~500 MB de herramientas)
- **Etapa 2 (runtime):** Solo incluye Java Alpine + el `.jar` generado (~180 MB final)

Esto garantiza que la imagen de producción **no contiene herramientas de desarrollo**,
reduciendo la superficie de ataque y el tamaño del contenedor.

---

## Ejecutar localmente con Docker

```bash
# Construir la imagen
docker build -t registraduria:local .

# Ejecutar el contenedor
docker run -p 8080:8080 registraduria:local

# Probar el endpoint de registro
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Ana","id":100,"age":30,"gender":"FEMALE","alive":true}'

# Respuesta esperada: VALID
```

---

## Reglas de negocio del sistema

| Condición | Resultado |
|-----------|-----------|
| Persona válida (≥18 años, viva, sin duplicados) | `VALID` |
| Persona fallecida (`alive=false`) | `DEAD` |
| Menor de edad (< 18 años) | `UNDERAGE` |
| Edad fuera de rango (< 0 o > 120) | `INVALID_AGE` |
| Documento ya registrado | `DUPLICATED` |
| Datos nulos | `INVALID` |

---

## Estructura del repositorio

```
registraduria/
├── .github/
│   └── workflows/
│       └── ci.yml              # Pipeline CI (GitHub Actions)
├── src/
│   ├── main/
│   │   └── java/edu/unisabana/tyvs/registry/
│   │       ├── application/    # Lógica de negocio (Registry)
│   │       ├── delivery/       # API REST (RegistryController)
│   │       ├── domain/         # Modelos (Person, Gender, RegisterResult)
│   │       └── infraestructure/ # Persistencia (RegistryRepository H2)
│   └── test/
│       └── java/               # Pruebas de integración y sistema
├── Dockerfile                  # Receta de la imagen Docker (multi-stage)
├── Jenkinsfile                 # Pipeline CD (Jenkins) — stages definidos
├── pom.xml                     # Dependencias Maven
└── README.md                   # Este archivo
```

---

## Autor

- **Jonathan Muñoz Vargas** — Universidad de La Sabana
- Curso: Fundamentos Devops
- Docente: María Fernanda Ochoa
- Actividad 3 Unidad 2: Laboratorio técnico
