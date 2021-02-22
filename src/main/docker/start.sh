#!/bin/sh


# Set Default Active Profiles
if [ -z "$SPRING_OPTS" ]; then
	export SPRING_OPTS='--spring.profiles.active=ssl'
fi

echo 'Sleeping 30 seconds to ensure DB is started'
sleep 30s

java \
-Djava.security.egd=file:/dev/./urandom \
-Drun.addResources="false" \
-jar /@project.build.finalName@.jar ${SPRING_OPTS}
