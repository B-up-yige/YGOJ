FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
COPY model/pom.xml model/
COPY services/pom.xml services/
COPY services/service-user/pom.xml services/service-user/
COPY services/service-problem/pom.xml services/service-problem/
COPY services/service-record/pom.xml services/service-record/
COPY services/service-judger/pom.xml services/service-judger/
COPY gateway/pom.xml gateway/
COPY fileSystem/pom.xml fileSystem/

RUN mvn dependency:go-offline -B

COPY model/src model/src
COPY services/service-user/src services/service-user/src
COPY services/service-problem/src services/service-problem/src
COPY services/service-record/src services/service-record/src
COPY services/service-judger/src services/service-judger/src
COPY gateway/src gateway/src
COPY fileSystem/src fileSystem/src

ARG SERVICE_NAME
RUN mvn clean package -pl ${SERVICE_NAME} -am -DskipTests -B

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

ARG SERVICE_NAME
ARG SERVICE_PORT
COPY --from=builder /build/${SERVICE_NAME}/target/*.jar app.jar

EXPOSE ${SERVICE_PORT}

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

ENTRYPOINT java ${JAVA_OPTS} -jar app.jar
