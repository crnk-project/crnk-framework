# crnk-framework (ixshel fork)

[![Build Status](https://github.com/ixshel/crnk-framework/actions/workflows/build.yml/badge.svg)](https://github.com/ixshel/crnk-framework/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellowgreen.svg)](LICENSE.txt)

## What is Crnk?

Crnk is an implementation of the [JSON API](https://jsonapi.org/) specification and recommendations in Java to
facilitate building RESTful applications. It provides many conventions and building blocks that application can benefit from.
This includes features such as  sorting, filtering, pagination, requesting complex object graphs, sparse
field sets, attaching links to data or atomically execute multiple operations. Further integration
with frameworks and libraries such as Spring, CDI, JPA, Bean Validation, Dropwizard, Servlet API, Zipkin and
and more ensure that JSON API plays well together with the Java ecosystem. Have a look at
[www.crnk.io](http://www.crnk.io) and the  [documentation](http://www.crnk.io/releases/stable/documentation/) for more detailed
information.

This repository is an `ixshel`-maintained fork of the original Crnk project. The fork keeps the original framework and
module layout, while updating the supported baseline to Java 21, Gradle 8.5, and Spring Boot 3 / Jakarta compatibility.

## Repository

Fork repository: https://github.com/ixshel/crnk-framework

Historical Crnk references to Bintray/JCenter are obsolete and do not apply to this fork. Build the project locally
or publish artifacts from your own release pipeline if you need fork-specific binaries.


## Requirements

This fork requires Java 21 for build and test execution and an SLF4J setup for logging.

## Example

See the original example project: https://github.com/crnk-project/crnk-example/

Gradle `settings.gradle` can look like:

```
gradle.beforeProject { Project project ->
    project.with {
        buildscript {
            repositories {
                mavenCentral()
            }
        }
        repositories {
            mavenCentral()
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

    implementation "io.crnk:crnk-setup-spring-boot3"
    implementation "io.crnk:crnk-data-jpa-jakarta"
    implementation "io.crnk:crnk-data-facet"
    implementation "io.crnk:crnk-format-plain-json"
    implementation "io.crnk:crnk-validation-jakarta"
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

Crnk integrates well with many frameworks. The upstream documentation at
[crnk.io](http://www.crnk.io/releases/stable/documentation/) is still a useful reference, but check this fork first for
Java 21, Gradle 8.5, and Spring Boot 3 / Jakarta-specific behavior.

## Licensing

Crnk is licensed under the Apache License, Version 2.0.
You can grab a copy of the license at http://www.apache.org/licenses/LICENSE-2.0.


## Building from Source

This fork uses Gradle for its build. To build the complete project run

    ./gradlew clean build

Use Java 21 when building or running the test suite.

Note as part of the build a local Node installation is downloaded to build the frontend parts (`crnk-ui`) of the project.


## Links

* [Fork repository](https://github.com/ixshel/crnk-framework/)
* [Fork issue tracker](https://github.com/ixshel/crnk-framework/issues)
* [Fork build](https://github.com/ixshel/crnk-framework/actions)
* [Original Crnk homepage](http://www.crnk.io)
* [Original Crnk documentation](http://www.crnk.io/releases/stable/documentation/)


## Endorsements

[![YourKit](https://www.yourkit.com/images/yklogo.png)](https://www.yourkit.com/youmonitor/)

We thank YourKit for supporting open source projects with profiler and monitoring tooling.
