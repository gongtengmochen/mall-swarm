FROM m.daocloud.io/docker.io/library/maven:3.9.9-eclipse-temurin-17 AS build

ARG MODULE_NAME

WORKDIR /workspace
COPY . .

RUN mvn -pl ${MODULE_NAME} -am clean package -DskipTests \
    && cp "$(find "${MODULE_NAME}/target" -maxdepth 1 -name "*.jar" ! -name "*original*" | head -n 1)" /tmp/app.jar

FROM m.daocloud.io/docker.io/library/eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /tmp/app.jar /app/app.jar

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
