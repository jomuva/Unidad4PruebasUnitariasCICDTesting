pipeline {
    agent any

    environment {

        DOCKERHUB_USER = 'jomuva'


        REPO_URL = 'https://github.com/jomuva/Unidad4PruebasUnitariasCICDTesting'


        DOCKER_IMAGE = "${DOCKERHUB_USER}/registraduria"


        DOCKER_TAG = "${BUILD_NUMBER}"


        REGISTRY_CREDS = 'dockerhub-credentials'
    }

    stages {

        // ── STAGE 1: Obtener el código fuente desde GitHub ──────────────
        stage('Clonar repositorio') {
            steps {
                git branch: 'main',
                    url: env.REPO_URL
                echo "✅ Código clonado desde: ${env.REPO_URL}"
            }
        }

        // ── STAGE 2: Construir la imagen Docker usando el Dockerfile ─────
        stage('Construir imagen Docker') {
            steps {
                script {
                    dockerImage = docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                    echo "✅ Imagen construida: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                }
            }
        }

        // ── STAGE 3: Publicar la imagen en DockerHub ─────────────────────
        stage('Publicar imagen en DockerHub') {
            steps {
                script {
                    docker.withRegistry(
                        'https://registry.hub.docker.com',
                        env.REGISTRY_CREDS
                    ) {

                        dockerImage.push(env.DOCKER_TAG)


                        dockerImage.push('latest')

                        echo "✅ Imagen publicada: ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                    }
                }
            }
        }


        stage('Verificar imagen publicada') {
            steps {
                sh """
                    echo "Verificando disponibilidad de la imagen en DockerHub..."
                    docker pull ${DOCKER_IMAGE}:latest
                    echo "✅ Imagen verificada correctamente"
                """
            }
        }
    }


    post {
        success {
            echo "✅ Pipeline CD completado — imagen disponible en DockerHub como ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
        }
        failure {
            echo "❌ Pipeline CD falló — revisar logs de cada stage"
        }
        always {

            sh "docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} || true"
        }
    }
}
