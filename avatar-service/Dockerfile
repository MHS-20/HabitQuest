FROM eclipse-temurin:21 AS builder
WORKDIR /workspace
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} avatar-service.jar
RUN java -Djarmode=layertools -jar avatar-service.jar extract

FROM eclipse-temurin:21-jre
WORKDIR /workspace
ARG OTEL_VERSION=2.23.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY --from=builder /workspace/dependencies/ ./
COPY --from=builder /workspace/spring-boot-loader/ ./
COPY --from=builder /workspace/snapshot-dependencies/ ./
COPY --from=builder /workspace/application/ ./
ENTRYPOINT ["java", "-javaagent:/otel/opentelemetry-javaagent.jar", "org.springframework.boot.loader.launch.JarLauncher"]