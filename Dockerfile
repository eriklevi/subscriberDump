FROM openjdk:8-jdk-alpine
MAINTAINER Erik Levi <levi.erik@gmail.com>
ADD target/subscriberDump-0.0.1-SNAPSHOT.jar subscriber-dump.jar
ENTRYPOINT ["java", "-jar", "/subscriber-dump.jar"]