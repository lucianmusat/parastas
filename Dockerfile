# Stage 1: Build the Maven project
FROM --platform=linux/amd64 maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn install

# Stage 2: Create the final Docker image.
FROM docker:24

RUN apk add --no-cache openjdk17

WORKDIR /app
COPY --from=build /app/target/Parastas-1.0.0-SNAPSHOT.jar .

CMD ["java", "-jar", "Parastas-1.0.0-SNAPSHOT.jar"]
