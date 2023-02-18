FROM bellsoft/liberica-openjdk-alpine:17
COPY app/target/app-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
