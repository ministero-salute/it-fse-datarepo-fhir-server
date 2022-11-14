FROM registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:1.8

WORKDIR /workspace/app

ARG JAR_FILE=./*.jar

ENV AB_JOLOKIA_OFF=true
ENV WORKBENCH_MAX_METASPACE_SIZE=1024
ENV JAVA_DIAGNOSTICS=true
ENV GC_MAX_METASPACE_SIZE=300

COPY ${JAR_FILE} /deployments/

USER jboss