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
        stage('Clean container stop') {
            steps {
                bat "docker-compose -p pet stop"
            }
        }
        stage('Clean container remove') {
            steps {
                bat "docker-compose -p pet rm -f"
            }
        }
        stage('Clean image remove') {
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
                bat 'docker-compose up --build'
                script {
                    def containerStatus = bat(script: 'docker inspect -f {{.State.Status}} pet-api', returnStdout: true).trim()
                    while (containerStatus != 'running') {
                        echo "Waiting for the container to start..."
                        bat 'timeout /t 10 > nul' // Подождать 10 секунд перед следующей попыткой
                        containerStatus = bat(script: 'docker inspect -f {{.State.Status}} pet-api', returnStdout: true).trim()
                    }
                    echo "Container is running."
                }
            }
        }
    }
}