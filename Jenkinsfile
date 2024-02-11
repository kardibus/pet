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
        stage('Container stop') {
            steps {
                bat "docker-compose -p pet stop"
            }
        }
        stage('Container remove') {
            steps {
                bat "docker-compose -p pet rm -f"
            }
        }
        stage('Image remove') {
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
                    waitForContainerToStart()
                }
            }
        }
    }
}

def waitForContainerToStart() {
    def attempts = 0
    while (attempts < 6) { // Попробуем 6 раз
        attempts++
        def containerStatus = bat(script: 'docker inspect -f {{.State.Status}} pet-api', returnStdout: true).trim()
        if (containerStatus == 'running') {
            echo "Container is running."
            return
        } else {
            echo "Container is not yet running. Attempt ${attempts}..."
            sleep 10 // Подождать 10 секунд перед следующей попыткой
        }
    }
    echo "Container did not start within the expected time."
}