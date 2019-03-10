# spring-boot-microservice-example

Showcases two minimal Spring Boot micro services. One service serves
projects, while the other tasks. A JSON:API relationship is introduced
between task and project that spans over the two underlying services.

The interesting piece is the `TaskProjectLinkerModule` doing the linking
from tasks to projects. Each service makes use of a `CrnkClient`-based
stub of the other service that allows to interact with the other service.
In doing so, one automatically gains JSON:API features like relationship links
and inclusions. In a real-world application the project microservice would have
to publish its API (the Project class) for the task microservice.

In order to run this example do:

## How to run with Gradle

	gradlew :crnk-integration-examples:spring-boot-microservice-example:run

## The service will be available at
 
 	http://localhost:8080/
