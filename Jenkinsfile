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
        stage('Clean container') {
            steps {
                bat "docker-compose -p pet-docker stop"
                bat "docker-compose -p pet-docker rm -f"
                bat "docker rmi pet-docker-api:latest"
            }
        }
        stage('Docker-compose start') {
            steps {
                bat 'docker-compose up --build'
            }
        }
    }
}