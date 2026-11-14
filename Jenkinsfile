// Pipeline CI/CD del Zoo Fantástico (Partes 5-7 del laboratorio).
// Jenkins corre en Windows, por eso se usa "bat" en lugar de "sh" y mvnw.cmd en lugar de mvn.
pipeline {
    agent any

    environment {
        IMAGE     = 'zoo-fantastic-app:latest'
        CONTAINER = 'zoo-fantastico-app'
        HOST_PORT = '8081'   // el 8080 del host lo ocupa otro proyecto
        // Contraseña de MySQL guardada en Jenkins (Credentials > Secret text, ID db-password)
        DB_PASSWORD = credentials('db-password')
        // Dentro del contenedor "localhost" es el propio contenedor: MySQL está en el host
        DB_URL = 'jdbc:mysql://host.docker.internal:3306/zoo_fantastico?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
    }

    stages {
        stage('Run Tests') {
            steps {
                // Si alguna prueba falla, el pipeline se detiene aquí y no se despliega
                bat 'mvnw.cmd clean test'
            }
        }
        stage('Build') {
            steps {
                // Las pruebas ya corrieron en la etapa anterior
                bat 'mvnw.cmd package -DskipTests'
            }
        }
        stage('Docker Build') {
            steps {
                bat 'docker build -t %IMAGE% .'
            }
        }
        stage('Docker Run') {
            steps {
                // Elimina el contenedor de la ejecución anterior (si existe) para liberar el puerto
                bat 'docker rm -f %CONTAINER% || exit 0'
                // -e DB_PASSWORD sin valor: Docker lo toma del entorno, sin escribirlo en el comando
                bat 'docker run -d --name %CONTAINER% -p %HOST_PORT%:8080 -e DB_PASSWORD -e SPRING_DATASOURCE_URL="%DB_URL%" %IMAGE%'
            }
        }
    }

    post {
        always {
            echo 'Pipeline terminado.'
        }
        success {
            echo "¡Despliegue exitoso! App en http://localhost:${env.HOST_PORT}/api/zones"
        }
        failure {
            echo 'Error en el pipeline. Revisar los logs.'
        }
    }
}
