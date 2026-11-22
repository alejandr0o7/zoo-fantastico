FROM jenkins/jenkins:lts-jdk21

USER root

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    maven \
    docker-cli \
    git \
    curl \
    ca-certificates && \
    rm -rf /var/lib/apt/lists/*

USER jenkins
