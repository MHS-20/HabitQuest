FROM eclipse-temurin:21-jdk-noble AS builder
WORKDIR /workspace
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} avatar-service.jar
RUN java -Djarmode=layertools -jar avatar-service.jar extract

FROM eclipse-temurin:21-jre-noble
RUN apt-get update && apt-get upgrade -y && apt-get clean

WORKDIR /workspace
ARG OTEL_VERSION=2.23.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar

COPY --from=builder /workspace/dependencies/ ./
COPY --from=builder /workspace/spring-boot-loader/ ./
COPY --from=builder /workspace/snapshot-dependencies/ ./
COPY --from=builder /workspace/application/ ./

RUN groupadd --system appgroup \
    && useradd --system --gid appgroup --no-create-home appuser \
    && chown -R appuser:appgroup /workspace /otel
USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/otel/opentelemetry-javaagent.jar", "org.springframework.boot.loader.launch.JarLauncher"]