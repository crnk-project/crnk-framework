# crnk.io - Crank up the development of RESTful applications!

<!--
currently broken: https://github.com/badges/shields/issues/658
https://img.shields.io/bintray/v/crnk-project/maven/crnk-core.svg

[![Maven Central](https://img.shields.io/maven-central/v/io.crnk/crnk-core.svg)](http://mvnrepository.com/artifact/io.crnk/crnk-core)

-->

[![Build Status](https://travis-ci.org/crnk-project/crnk-framework.svg?branch=master)](https://travis-ci.org/crnk-project/crnk-framework)
[![Gitter](https://img.shields.io/gitter/room/crkn-io/lobby.svg)](https://gitter.im/crnk-io/Lobby)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellowgreen.svg)](https://github.com/crnk-project/crnk-framework/blob/master/LICENSE.txt)
[![Coverage Status](https://coveralls.io/repos/github/crnk-project/crnk-framework/badge.svg?branch=master)](https://coveralls.io/github/crnk-project/crnk-framework?branch=master)

[![Bintray](https://img.shields.io/bintray/v/crnk-project/maven/crnk-core.svg)](https://bintray.com/crnk-project/maven/crnk-core) release on jcenter\
[![Bintray](https://img.shields.io/bintray/v/crnk-project/mavenLatest/crnk-core.svg)](https://bintray.com/crnk-project/mavenLatest/crnk-core) latest in private repository


## What is Crnk?

Crnk is an implementation of the [JSON API](https://http://jsonapi.org/) specification and recommendations in Java to
facilitate building RESTful applications. It provides many conventions and building blocks that application can benefit from.
This includes features such as  sorting, filtering, pagination, requesting complex object graphs, sparse
field sets, attaching links to data or atomically execute multiple operations. Further integration
with frameworks and libraries such as Spring, CDI, JPA, Bean Validation, Dropwizard, Servlet API, Zipkin and
and more ensure that JSON API plays well together with the Java ecosystem. Have a look at
[www.crnk.io](http://www.crnk.io) and the  [documentation](http://www.crnk.io/releases/stable/documentation/) for more detailed
information.

Release notes can be found in http://www.crnk.io/releases/.

## Repository

Crnk Maven artifacts are available from jcenter/bintray: <a href="https://bintray.com/crnk-project">https://bintray.com/crnk-project</a>.

Note that due to reliability issues of MavenCentral we only rarely publish there.


## Requirements

Crnk requires Java 1.8 or later and an SLF4J setup for logging.

## Example

See https://github.com/crnk-project/crnk-example/

Gradle `settings.gradle` can look like:

```
gradle.beforeProject { Project project ->
    project.with {
        buildscript {
            repositories {
                jcenter()
                // maven { url 'https://dl.bintray.com/crnk-project/mavenLatest/' }
            }
        }
        repositories {
            jcenter()
            // maven { url 'https://dl.bintray.com/crnk-project/mavenLatest/' }
        }
    }
}
```

and the `build.gradle`:

```
dependencies {
    implementation platform('io.crnk:crnk-bom:INSERT_VERSION_HERE')
    annotationProcessor platform('io.crnk:crnk-bom:INSERT_VERSION_HERE')

    annotationProcessor 'io.crnk:crnk-gen-java'

    implementation "io.crnk:crnk-setup-spring-boot2"
    implementation "io.crnk:crnk-data-jpa"
    implementation "io.crnk:crnk-data-facet"
    implementation "io.crnk:crnk-format-plain-json"
    implementation "io.crnk:crnk-validation"
    implementation "io.crnk:crnk-home"
    implementation "io.crnk:crnk-ui"
    implementation "io.crnk:crnk-operations"
    implementation "io.crnk:crnk-security"
}
```

and a basic Java example:

```
@JsonApiResource(type = "vote")
@Data
public class Vote {

    @JsonApiId
    private UUID id;

    private int stars;

}

public class VoteRepository extends ResourceRepositoryBase<Vote, UUID> {

    public Map<UUID, Vote> votes = new ConcurrentHashMap<>();

    public VoteRepository() {
        super(Vote.class);
    }

    @Override
    public ResourceList<Vote> findAll(QuerySpec querySpec) {
        return querySpec.apply(votes.values());
    }

    @Override
    public <S extends Vote> S save(S entity) {
        votes.put(entity.getId(), entity);
        return null;
    }

    @Override
    public void delete(UUID id) {
        votes.remove(id);
    }
}
```

or with JPA:

```
@JsonApiResource(type = "person")
@Entity
@Data
public class PersonEntity {

	@Id
	private UUID id;

	private String name;

	private int year;

	@OneToMany(mappedBy = "movie")
	private List<RoleEntity> roles = new ArrayList<>();

	@Version
	private Integer version;
}

public class PersonRepository extends JpaEntityRepositoryBase<PersonEntity, UUID> {

	public PersonRepository() {
		super(PersonEntity.class);
	}

	@Override
	public PersonEntity save(PersonEntity entity) {
		// add your save logic here
		return super.save(entity);
	}

	@Override
	public PersonEntity create(PersonEntity entity) {
		// add your create logic here
		return super.create(entity);
	}

	@Override
	public void delete(UUID id) {
		// add your save logic here
		super.delete(id);
	}
}
```

Crnk integrates well with many frameworks. Have a look
at the  [documentation](http://www.crnk.io/releases/stable/documentation/)
and carefully choose what you need. Don't hesitate to ask for help and suggest
improvements!

## Licensing

Crnk is licensed under the Apache License, Version 2.0.
You can grab a copy of the license at http://www.apache.org/licenses/LICENSE-2.0.


## Building from Source

Crnk make use of Gradle for its build. To build the complete project run

    gradlew clean build

Note as part of the build a local Node installation is downloaded to build the frontend parts (crnk-ui) of the project.


## Links

* [Homepage](http://www.crnk.io)
* [Documentation](http://www.crnk.io/releases/stable/documentation/)
* [Source code](https://github.com/crnk-project/crnk-framework/)
* [Issue tracker](https://github.com/crnk-project/crnk-framework/issues)
* [Forum](https://gitter.im/crnk-io/Lobby)
* [Build](https://travis-ci.org/crnk-project/crnk-framework/)


## Endorsements

[![YourKit](https://www.yourkit.com/images/yklogo.png)](https://www.yourkit.com/youmonitor/)

We thank YourKit for supporting open source projects with profiler and monitoring tooling.
