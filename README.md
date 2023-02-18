### About 
Demo application showing how to implement batch processing using Spring Batch. <br>

### Requirements
 - Java 17
 - docker (for building and running application)

### Building & running docker image

`docker build -t sample-batch-app .`<br>
`docker run -p 8080:8080 sample-batch-app`<br>

TODO add support of passing application.properties to application in container 

### Usage with external HyperSQL
Download database engine from https://www.hsqldb.org/ and run it with:
```shell
java -cp lib/hsqldb.jar org.hsqldb.server.WebServer --database.0 file:mydb --dbname.0 xdb
```
After that use can connect to database using db console by connection URL `jdbc:hsqldb:http://localhost/xdb`. <br>
User: sa, no password.<br>
Before start application you should specify profile `useExternalHSQLDB`.<br>
It's mandatory to specify `spring.batch.jdbc.initialize-schema=always` 
and `spring.sql.init.mode=always` at first run to allow spring create tables.