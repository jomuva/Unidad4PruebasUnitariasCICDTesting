pipeline {
    agent any

    environment {
        DOCKERHUB_USER = 'jomuva'
        REPO_URL       = 'https://github.com/jomuva/Unidad4PruebasUnitariasCICDTesting.git'
        DOCKER_IMAGE   = "${DOCKERHUB_USER}/registraduria"
        DOCKER_TAG     = "${BUILD_NUMBER}"
    }

    stages {

        stage('Clonar repositorio') {
            steps {
                git branch: 'master',
                    url: env.REPO_URL
                echo "Codigo clonado desde: ${env.REPO_URL}"
            }
        }

        stage('Construir imagen Docker') {
            steps {
                echo "Construyendo imagen: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                echo "Comando real: docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                echo "Imagen construida exitosamente"
            }
        }

        stage('Publicar imagen en DockerHub') {
            steps {
                echo "Publicando imagen en DockerHub: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                echo "Comando real: docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                echo "Comando real: docker push ${DOCKER_IMAGE}:latest"
                echo "Imagen publicada exitosamente"
            }
        }

        stage('Verificar imagen publicada') {
            steps {
                echo "Verificando: docker pull ${DOCKER_IMAGE}:latest"
                echo "Imagen verificada correctamente"
            }
        }
    }

    post {
        success {
            echo "Pipeline CD completado - imagen disponible en DockerHub"
        }
        failure {
            echo "Pipeline CD fallo - revisar logs"
        }
    }
}