# dagger-vertx-example

This example showcases a very light-weight flavor of setting up crnk:

- Vert.x to serve HTTP requests.
- Logback for logging.
- Dagger for perform dependency injection: evaluated at compile-time without any needs for expensive runtime lookup as e.g. CDI or Spring.
- Proguard for shrink the application and third-party libraries into `dagger-vertx-example-min-<version>.jar`
  saving about 60% of space.
- Docker to package the application (using the bmuschko gradle plugin).
- openj9-based openjdk 10 base image to (drastically) reduce startup time and memory usage.  
- Reactor for reactive programming.
  
The resulting is 4.5 MB in size and uses 35 MB of memory (excluding 
Docker base image subject to optimization with jlink/alpine).

Note that Vert.x and Reactor integration are **still considered being experimental** and 
do not support the full crnk feature set! But a similar non-reactive setup is equally 
possible by replacing Vert.x with an embedded servlet container.

## How to run 

Make sure to make use of Java 10 when compiling the examples. Proguard gets disabled for Java 8
(and Java 9 has not been tested). 

To run the minimized version use:

	gradlew :crnk-examples:dagger-vertx-example:runMin
	
To run from an IDE launch:
	
    `VertxApplication` main application.	
    
To launch with docker:

    gradlew :crnk-examples:dagger-vertx-example:dockerBuildImage
    docker run --name=crnk -p 8080:8080 crnk/example-dagger-vertx:0.0.0-SNAPSHOT
    docker stats
    

## The service will be available at
 
 	http://localhost:8080/projects
 	
 	
## Future Work

Note that jigsaw support by Gradle and others is currently rather limited. As such jaotc and jlink
have not yet been used by this example. Upcoming work once better support is available to
further optimize the application: 

- Setup jlink to reduce the size of the distribution.
- alpine as base image.
- Package into a docker image.
- Improve tree-shaking with proguard. 
- Setup jaotc for ahead-of-time compilation.
 