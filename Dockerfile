FROM openjdk:8-jdk-alpine

#ENV VERSION=@project.version@

#LABEL version="@project.version@"\
#    maintainer="apfes1@gmail.com"

# Copy the Spring Boot "uberjar" that is built by Maven into the Docker image
COPY target/*.jar app.jar
ADD start.sh .

# Install prereq's
RUN chmod +x start.sh

CMD ["./start.sh"]