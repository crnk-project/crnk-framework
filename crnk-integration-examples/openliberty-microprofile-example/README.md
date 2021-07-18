Adapted from wildfly-example.

This is a MicroProfile 4 example using Open Liberty as the runtime 'engine'.

Tested using Java 11.

In order to run this example do (at this example folder):
* ```../../gradlew clean libertyPackage```
* ```java -jar build/libs/MyPackage.jar```
* The service will be available at:
  - http://localhost:9080/openliberty-microprofile-example/api/users
  - http://localhost:9080/openliberty-microprofile-example/api/schedule
