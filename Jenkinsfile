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
                bat "docker-compose -p pet-docker stop"
            }
        }
        stage('Clean container remove') {
            steps {
                bat "docker-compose -p pet-docker rm -f"
            }
        }
        stage('Clean image remove') {
            steps {
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