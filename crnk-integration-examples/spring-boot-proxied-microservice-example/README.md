# spring-boot-proxied-microservice-example

Builds on the Spring boot microservice examples and removes the need for the consuming service 
have any knowledge of the structure of the remote resource.

One service serves projects, while the other tasks. A JSON:API relationship is introduced
between task and project that spans over the two underlying services.

The Task Service using a 'Proxy Resource' that enables it to report the linked Project. 
Known properties are mapped. Not matching properties are added to the attributes map. 



In order to run this example do:

## How to run with Gradle

	gradlew :crnk-integration-examples:spring-boot-proxied-microservice-example:run

## The service will be available at
 
 	http://localhost:8080/
