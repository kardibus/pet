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
                bat 'docker ps -a --format {{.Names}} | Select-String "pet" | ForEach-Object { docker stop $_.ToString().Trim() }'
                bat 'docker ps -a --format {{.Names}} | Select-String "pet" | ForEach-Object { docker rm $_.ToString().Trim() }'
                bat 'docker images --format {{.Repository}}:{{.Tag}} | Select-String "pet-api" | ForEach-Object { docker rmi $_.ToString().Trim() }'
            }
        }
        stage('Docker-compose start') {
            steps {
                bat 'docker compose up -d'
            }
        }
    }
}
