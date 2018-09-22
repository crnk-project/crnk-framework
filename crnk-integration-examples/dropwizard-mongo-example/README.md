# dropwizard-mongo-example

In order to run this example do:

## Update configuration

Update configuration.yml to point to your MongoDB installation.

## How to run with Gradle

	gradlew :crnk-integration-examples:dropwizard-mongo-example:run

## The service will be available at
 
 	http://localhost:8080/projects

## Disclaimer

The use of MongoDB makes this example difficult to test in a
Java environment. The example may get replaced in the future.
A real implementation would also rather follow the crnk-jpa
approach by providing a generic MongoResourceRepository and
register it for each MongoDB entity type with the module API.

