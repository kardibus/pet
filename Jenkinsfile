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
                bat "docker rmi pet-api:latest"
            }
        }
        stage('Docker-compose start') {
            steps {
                bat 'docker-compose up --build'
            }
        }
    }
}