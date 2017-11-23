# crnk.io - Crank up the development of RESTful applications!


[![Maven Central](https://img.shields.io/maven-central/v/io.crnk/crnk-core.svg)](http://mvnrepository.com/artifact/io.crnk/crnk-core)
[![Build Status](https://travis-ci.org/crnk-project/crnk-framework.svg?branch=master)](https://travis-ci.org/crnk-project/crnk-framework)
[![Gitter](https://img.shields.io/gitter/room/crkn-io/lobby.svg)](https://gitter.im/crnk-io/Lobby)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellowgreen.svg)](https://github.com/crnk-project/crnk-framework/blob/master/LICENSE.txt)
[![Coverage Status](https://coveralls.io/repos/github/crnk-project/crnk-framework/badge.svg?branch=master)](https://coveralls.io/github/crnk-project/crnk-framework?branch=master)


## What is Crnk?

Crnk is an implementation of the [JSON API](https://http://jsonapi.org/) specification and recommendations in Java to 
facilitate building RESTful applications. It provides many conventions and building blocks that application can benefit from. 
This includes features such as  sorting, filtering, pagination, requesting complex object graphs, sparse 
field sets, attaching links to data or atomically execute multiple operations. Further integration 
with frameworks and libraries such as Spring, CDI, JPA, Bean Validation, Dropwizard, Servlet API, Zipkin and
and more ensure that JSON API plays well together with the Java ecosystem. Have a look at 
[www.crnk.io](http://www.crnk.io) and the [documentation](http://www.crnk.io/documentation/) for more detailed 
information.

## Requirements

Crnk requires Java 1.7 or later. 

## Licensing

Crnk is licensed under the Apache License, Version 2.0.
You can grab a copy of the license at http://www.apache.org/licenses/LICENSE-2.0.


## Building from Source

Crnk make use of Gradle for its build. To build the complete project run

    gradlew clean build
    
Note as part of the build a local Node installation is downloaded to build the frontend parts (crnk-ui) of the project.    


## Links

* [Homepage](http://www.crnk.io)
* [Documentation](http://www.crnk.io/documentation)
* [Source code](https://github.com/crnk-project/crnk-framework/)
* [Issue tracker](https://github.com/crnk-project/crnk-framework/issues)
* [Forum](https://gitter.im/crnk-io/Lobby)
* [Build](https://travis-ci.org/crnk-project/crnk-framework/)