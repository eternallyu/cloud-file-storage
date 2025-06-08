FROM amazoncorretto:21 AS builder
WORKDIR /app

COPY gradlew .
COPY gradle/wrapper gradle/wrapper
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew \
    && ./gradlew dependencies --no-daemon

COPY src/ src/

RUN ./gradlew clean build -x test --no-daemon

FROM amazoncorretto:21
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar ./app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
