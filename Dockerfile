FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

ARG SERVICE_NAME
ARG SERVICE_PORT
COPY ${SERVICE_NAME}/target/*.jar app.jar

EXPOSE ${SERVICE_PORT}

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

ENTRYPOINT java ${JAVA_OPTS} -jar app.jar
