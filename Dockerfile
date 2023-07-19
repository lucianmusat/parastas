# Stage 1: Build the Maven project
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean install

# Stage 2: Create the final Docker image
FROM ubuntu:22.04

# Install Java
RUN apt update && apt install -y openjdk-17-jdk

# Install Docker
RUN apt-get update && apt-get install -y apt-transport-https ca-certificates curl software-properties-common wget
RUN wget -q -O - https://download.docker.com/linux/ubuntu/gpg | apt-key add -
RUN add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
RUN apt-get update && apt-get install -y docker-ce

# Create a docker group and add a user to it
RUN getent group docker || groupadd -g 999 docker \
    && usermod -aG docker root

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

WORKDIR /app
COPY --from=build /app/target/Parastas-0.0.1-SNAPSHOT.jar .

CMD ["java", "-jar", "Parastas-0.0.1-SNAPSHOT.jar"]
