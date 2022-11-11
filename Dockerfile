FROM gradle:jdk17 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM openjdk:17-alpine

EXPOSE 8080

RUN mkdir /app

COPY --from=builder /home/gradle/src/build/libs/helpingfriendlybook.jar
/application.jar

ENTRYPOINT ["java", "-jar", "application.jar"]
