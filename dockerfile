FROM maven:3.9-amazoncorretto-25-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests -B

FROM amazoncorretto:25-alpine AS jre-builder
COPY --from=builder /app/target/*.jar /file-management-service-0.0.1-SNAPSHOT.jar

RUN mkdir /deps && cd /deps && \
    jar -xf /file-management-service-0.0.1-SNAPSHOT.jar BOOT-INF/lib && \
    jdeps --ignore-missing-deps --print-module-deps --multi-release 25 \
    --class-path 'BOOT-INF/lib/*' \
    /file-management-service-0.0.1-SNAPSHOT.jar > /modules.txt

RUN jlink \
    --add-modules $(cat /modules.txt) \
    --strip-java-debug-attributes \
    --no-man-pages \
    --no-header-files \
    --output /custom-jre

FROM alpine:3.24
RUN apk add --no-cache ca-certificates

COPY --from=jre-builder /custom-jre /opt/java
COPY --from=builder /app/target/*.jar /app/file-management-service-0.0.1-SNAPSHOT.jar

ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN addgroup -S wc_secure_role && adduser -S wc_dev -G wc_secure_role
USER wc_dev

EXPOSE 8081
CMD ["java", "-jar", "/app/file-management-service-0.0.1-SNAPSHOT.jar"]