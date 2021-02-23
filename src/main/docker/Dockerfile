FROM openjdk:11-jre-slim

ENV VERSION=@project.version@

LABEL version="@project.version@"\
    maintainer="apfes1@gmail.com"\
    description="MixedNutz App Server"
LABEL org.label-schema.schema-version="1.0"
LABEL org.label-schema.build-date="@timestamp@"
LABEL org.label-schema.name="mixednutz/mixednutz-app-server"
LABEL org.label-schema.description="MixedNutz App Server"
LABEL org.label-schema.vcs-url="https://github.com/mixednutz/mixednutz-app-server"
LABEL org.label-schema.version="@project.version@"

# Copy the script and Spring Boot "uberjar" that is built by Maven into the Docker image
ADD start.sh .
ADD @project.build.finalName@.jar .

# Install prereq's
RUN chmod +x start.sh

CMD ["./start.sh"]