FROM gradle:8-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew shadowJar --no-daemon

FROM openjdk:21
EXPOSE 8080
WORKDIR /app
# Copy the versioned shadow jar to a stable runtime name so the entrypoint survives version changes.
COPY --from=build /home/gradle/src/build/libs/server-*-all.jar /app/server.jar
ENTRYPOINT ["java","-jar","/app/server.jar"]
