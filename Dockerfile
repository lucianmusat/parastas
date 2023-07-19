# Stage 1: Build the Maven project
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn install

# Stage 2: Create the final Docker image. Use raspbian to be able to run on Raspberry Pi
FROM docker:24

RUN apk add --no-cache openjdk17

WORKDIR /app
COPY --from=build /app/target/Parastas-0.0.1-SNAPSHOT.jar .

CMD ["java", "-jar", "Parastas-0.0.1-SNAPSHOT.jar"]
