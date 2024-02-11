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
//         stage('Clean container') {
//             steps {
//                 bat "docker ps -a -f name=${dockerContainerName} -q | ForEach-Object { docker stop $_ }"
//                 bat "docker ps -a -f name=${dockerContainerName} -q | ForEach-Object { docker rm $_ }"
//                 bat "docker images -q --filter=reference=${dockerImageName} | ForEach-Object { docker rmi -f $_ }"
//             }
//         }
        stage('Docker-compose start') {
            steps {
                bat 'docker-compose up --build'
            }
        }
    }
}