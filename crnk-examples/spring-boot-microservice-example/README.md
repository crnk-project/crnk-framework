# spring-boot-micro-service-example

Showcases two minimal Spring Boot micro services for projects and
tasks that have a JSON API relationship
between each each other and can make use of features like proper linking and
inclusions. The interesting piece is the TaskProjectLinkerModule doing the linking
from tasks to projects. In a real-world application the project microservice would have
to publish its API (the Project class) for the task microservice.

In order to run this example do:

## How to run with Gradle

	gradlew :crnk-examples:spring-boot-micro-service-example:run

## The service will be available at
 
 	http://localhost:8080/
