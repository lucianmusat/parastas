# Use the Ubuntu 22.04 as the base image
FROM ubuntu:22.04

# Update the package lists
RUN apt-get update

# Install Java 17
RUN apt-get install -y openjdk-17-jdk

# Install Docker
RUN apt-get install -y apt-transport-https ca-certificates curl software-properties-common
RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
RUN add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
ARG SMTP_USERNAME
ARG SMTP_PASSWORD
ARG RECIPIENT_EMAIL

RUN apt-get update
RUN apt-get install -y docker-ce

# Create a docker group and add a user to it
RUN getent group docker || groupadd -g 999 docker \
    && usermod -aG docker root

# Set the JAVA_HOME environment variable
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

WORKDIR /app

COPY target/Parastas-0.0.1-SNAPSHOT.jar /app

CMD ["java", "-jar", "Parastas-0.0.1-SNAPSHOT.jar"]
