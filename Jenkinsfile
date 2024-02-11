pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                withMaven(maven: 'MAVEN') {
                    bat 'mvn -e clean install'
                }
            }
        }
        stage('Clean container') {
            steps {
                bat "docker ps -f name=pet -q | xargs --no-run-if-empty docker container stop"
                bat "docker container ls -a -f name=pet -q | xargs -r docker container rm"
                bat "docker images -q --filter=reference=pet-api | xargs --no-run-if-empty docker rmi -f"
            }
        }
        stage('Docker-compose start') {
            steps {
                bat 'docker compose up -d'
            }
        }
    }
}