FROM amazoncorretto:21-alpine
COPY ./build/libs/super-financer-backend-all.jar /tmp/server.jar
WORKDIR /tmp
ENTRYPOINT ["java","-jar","server.jar"]