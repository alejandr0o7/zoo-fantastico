# Imagen base con solo el JRE de Java 21 (el proyecto compila con Java 21;
# la imagen openjdk:11-jre-slim del enunciado no puede ejecutar clases de Java 21).
FROM eclipse-temurin:21-jre

WORKDIR /app

# El JAR lo genera "mvnw clean package" (finalName = zoo-fantastic-app en el pom.xml)
COPY target/zoo-fantastic-app.jar zoo-fantastic-app.jar

# Dentro del contenedor la app escucha en el 8080 (en el host se publica en el 8081)
EXPOSE 8080

CMD ["java", "-jar", "zoo-fantastic-app.jar"]
