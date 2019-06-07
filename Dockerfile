FROM openjdk:8-jdk-alpine

# Copy the Spring Boot "uberjar" that is built by Maven into the Docker image
COPY target/*.jar app.jar

# Add any JVM parameters here
ENV JAVA_OPTS=""

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]