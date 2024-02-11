FROM openjdk:17

WORKDIR /app

# Копирование JAR-файла в контейнер
COPY . .

ENTRYPOINT ["java", "-jar", "target//pet-0.0.1-SNAPSHOT.jar"]

EXPOSE 8082