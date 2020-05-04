FROM openjdk:8-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
ENV JAVA_OPTS="-XX:+UseContainerSupport"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar" ]