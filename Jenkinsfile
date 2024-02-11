pipeline {
    agent any
    environment {
        dockerContainerName = 'pet'
        dockerImageName = 'pet-api'
    }
    stages {
        stage('Build') {
            steps {
                withMaven(maven: 'MAVEN') {
                    bat 'mvn clean install'
                }
            }
        }
        stage('Clean container') {
            steps {
                bat "docker ps -a -f name=${dockerContainerName} -q | ForEach-Object { docker stop }"
                bat "docker ps -a -f name=${dockerContainerName} -q | ForEach-Object { docker rm }"
                bat "docker images -q --filter=reference=${dockerImageName} | ForEach-Object { docker rmi -f }"
            }
        }
        stage('Docker-compose start') {
            steps {
                bat 'docker compose up -d'
            }
        }
    }
}