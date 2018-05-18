# dagger-vertx-example

This examples shows a very light-weight flavor of setting up crnk:

- Vert.x to serve the HTTP requests.
- Logback for logging.
- Dagger to perform dependency injection (evaluated at compile-time without any needs for expensive runtime lookup as e.g. CDI or Spring)
- Proguard to shrink the required libraries.

## How to run with Gradle

	gradlew :crnk-examples:dagger-vertx-example:run
	
## How to run with an IDE

Start `VertxApplication` main application.	

## The service will be available at
 
 	http://localhost:8080/projects
 	
 	
## Feture Work

- Setup jaotc for ahead-of-time compilation.
- Setup jlink to reduce the size of the distribution.
- Package into a docker image.
 	
