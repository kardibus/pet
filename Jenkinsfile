pipeline {
    agent any
    environment {
        dockerContainerName = 'pet-api'
        dockerImageName = 'pet-image'
    }
    stages {
        stage('Build') {
            steps {
                withMaven(maven: 'MAVEN') {
                    bat 'mvn clean install'
                }
            }
        }
        stage('container stop') {
            steps {
                bat "docker-compose -p pet stop"
            }
        }
        stage('container remove') {
            steps {
                bat "docker-compose -p pet rm -f"
            }
        }
        stage('image remove') {
            steps {
               script {
                   def imageExists = bat(script: 'docker image inspect pet-api:latest > nul 2>&1', returnStatus: true) == 0
                   if (imageExists) {
                       bat "docker rmi pet-api:latest"
                   } else {
                       echo "Image pet-api:latest does not exist. Skipping clean-up."
                   }
               }
            }
        }
        stage('Docker-compose start') {
            steps {
                bat 'docker-compose up --build -d'
                script {
                    echo "Waiting for the container to start..."
                    bat 'timeout /t 60 > nul' // Подождать 60 секунд для запуска контейнера

                    def containerStatus = bat(script: 'docker inspect -f {{.State.Status}} pet-api', returnStdout: true).trim()
                    def attempts = 0
                    while (containerStatus != 'running' && attempts < 6) { // Попробуем 6 раз в течение 60 секунд
                        attempts++
                        echo "Container is not yet running. Attempt ${attempts}..."
                        bat 'timeout /t 10 > nul' // Подождать 10 секунд перед следующей попыткой
                        containerStatus = bat(script: 'docker inspect -f {{.State.Status}} pet-api', returnStdout: true).trim()
                    }
                    if (containerStatus == 'running') {
                        echo "Container is running."
                    } else {
                        echo "Container did not start within the expected time."
                    }
                }
            }
        }
    }
}