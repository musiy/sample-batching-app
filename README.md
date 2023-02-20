### About 
Demo application showing how to implement batch processing using Spring Batch. <br>

### Requirements
 - Java 17
 - docker (for building and running application)

### Building and running application
Building `mvn clean install`<br>
Running `cd app && java -jar target/app-0.0.1-SNAPSHOT.jar.jar` 

### Building & running docker image

`docker build -t sample-batch-app .`<br>
`docker run -p 8080:8080 sample-batch-app`<br>

TODO add support of passing application.properties to application in container

###  Usage

##### obtain file from url (default is https://jsonplaceholder.typicode.com/users)
`curl "localhost:8080/run/getCvsFromJson"`<br>
##### get lottery winner
`curl "localhost:8080/run/lotteryWinnerJob?fileWithUserAmounts=data.csv"`

### Usage with external HyperSQL
Download database engine from https://www.hsqldb.org/ and run it with:
```shell
java -cp lib/hsqldb.jar org.hsqldb.server.WebServer --database.0 file:mydb --dbname.0 xdb
```
After that use can connect to database using db console by connection URL `jdbc:hsqldb:http://localhost/xdb`. <br>
User: sa, no password.<br>
Before start application you should specify profile `useExternalHSQLDB`.<br>
It's mandatory to specify `spring.batch.jdbc.initialize-schema=always` 
and `spring.jpa.hibernate.ddl-auto=create` at first run to allow spring create tables.

```shell
cd app
# first run
java -Dspring.profiles.active="useExternalHSQLDB" -Dspring.jpa.hibernate.ddl-auto=create -Dspring.batch.jdbc.initialize-schema=always -jar target/app-0.0.1-SNAPSHOT.jar.jar
# following run
java -Dspring.profiles.active="useExternalHSQLDB" -jar target/app-0.0.1-SNAPSHOT.jar.jar
```
