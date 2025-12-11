# Usa un'immagine base Java 23
FROM eclipse-temurin:23-jdk-alpine

# Cartella di lavoro nel container
WORKDIR /app

# Copia il jar generato da Maven
COPY target/GestorePercorsi-1.0.jar GestorePercorsi.jar

# Esponi la porta su cui gira l’API REST
EXPOSE 8080

# Comando per avviare l’app
ENTRYPOINT ["java", "-jar", "GestorePercorsi.jar"]