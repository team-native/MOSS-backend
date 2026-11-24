FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

COPY gradle/ gradle/
COPY gradlew settings.gradle build.gradle ./
COPY src/ src/

RUN sed -i 's/\r$//' gradlew \
    && sh gradlew --no-daemon bootJar \
    && cp build/libs/*.jar /app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
