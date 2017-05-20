# Release Process

## Creating a release branch

Create a release branch from the develop branch.

        $ git checkout -b release-0.1.1 develop
        Switched to a new branch "release-0.1.1"
        $ mvn versions:set -DnewVersion="0.1.1"
        $ git commit -a -m "Bumped version number to 0.1.1"
        [release-0.1.1 74d9424] Bumped version number to 0.1.1
        1 files changed, 1 insertions(+), 1 deletions(-)

## Finishing a release branch

        $ git checkout master
        Switched to branch 'master'
        $ git merge --no-ff release-0.1.1
        Merge made by recursive.
        (Summary of changes)
        $ git tag -a 0.1.1

Need to merge those back into develop to merge hardening changes:

        $ git checkout develop
        Switched to branch 'develop'
        $ git merge --no-ff release-0.1.1
        Merge made by recursive.
        (Summary of changes)

## Deleting the release branch

        $ git branch -d release-0.1.1
        Deleted branch release-0.1.1 (was ff452fe)


## Deploying to Maven Central

        $ git checkout master
        $ mvn -Possrh-release clean deploy


## Deploying to Sonatype Snapshot Repository

NOTE: The develop branch MUST have '-SNAPSHOT' version!!!

        $ git checkout develop
        $ mvn -Possrh-release clean deploy
        (After inspecting the staging repository content at https://oss.sonatype.org/)
        $ mvn -Possrh-release nexus-staging:release

If you find something went wrong you can drop the staging repository with

        $ mvn nexus-staging:drop


You should add the following repository configuration in other projects to use the snapshot dependency:

        <repository>
          <id>sonatype-nexus-snapshots</id>
          <name>Sonatype Nexus Snapshots</name>
          <url>http://oss.sonatype.org/content/repositories/snapshots</url>
          <releases>
            <enabled>false</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>


## References
- http://nvie.com/posts/a-successful-git-branching-model/#finishing-a-release-branch
- http://central.sonatype.org/pages/apache-maven.html
