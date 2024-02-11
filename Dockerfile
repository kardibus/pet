FROM openjdk:17

WORKDIR /app

ARG JAR_FILE=target\pet-*.jar

COPY ${JAR_FILE} pet.jar

CMD ["java", "-jar", "-Xmx1024m", "/app/pet.jar"]

EXPOSE 8082