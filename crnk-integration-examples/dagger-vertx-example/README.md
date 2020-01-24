# dagger-vertx-example

This example showcases a very light-weight flavor of setting up crnk with:

- 4.5 MB in size (excluding JRE docker base image)
- 35 MB memory usage
- super fast startup time: below 1000ms for the application and 
  another 500 to 1000ms for the VM. Subject for improvement (see future work).

The examples makes use of:

- https://vertx.io/ to serve HTTP requests.
- openj9-based docker image to (drastically) reduce memory usage and startup time.  
- https://google.github.io/dagger/ for perform compile-time dependency injection.
- https://www.guardsquare.com/en/proguard to shrink the application.
- Reactor for reactive programming.

Note that Vert.x and Reactor integration are **still considered being experimental** and 
do not support the full crnk feature set. Setting up a equivalent non-reactive setup is 
easily possible as well by replacing Vert.x with an embedded servlet container.

## How to run 

Make sure to use Java 10+ to compile and run the example.

Gradle is used to build `dagger-vertx-example-min-<version>.jar` library. It contains
the application and third-party libraries that then get packaged into a docker image.

To run the minimized version use:

	gradlew :crnk-integration-examples:dagger-vertx-example:runMin
	
To run from an IDE launch:
	
    `VertxApplication` main application.	
    
To launch with docker:

    gradlew :crnk-integration-examples:dagger-vertx-example:dockerBuildImage
    docker run --name=crnk -p 8080:8080 crnk/example-dagger-vertx:0.0.0-SNAPSHOT
    docker stats
    

## The service will be available at
 
 	http://localhost:8080/projects
 	
 	
## Future Work

Note that there is so far only limited jigsaw support by Gradle, making the use of 
jaotc and jlink overly complicated. Once proper support is in place, it will 
allow further optimizations:

- Setup jlink to reduce the size of the distribution.
- alpine as base image.
- Package into a docker image.
- Improve tree-shaking with proguard. 
- Setup jaotc for ahead-of-time compilation.
 