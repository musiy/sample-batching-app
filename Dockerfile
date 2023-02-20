FROM bellsoft/liberica-openjdk-alpine:17
COPY app/target/app-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
