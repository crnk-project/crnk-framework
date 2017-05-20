#!/bin/bash
set -ev

if [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
    echo 'Internal pull request: trigger QA and analysis'

    mvn org.jacoco:jacoco-maven-plugin:prepare-agent sonar:sonar \
        $MAVEN_OPTIONS \
        -Dsource.skip=true \
        -Dsonar.analysis.mode=preview \
        -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
        -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
        -Dsonar.github.oauth=$GITHUB_TOKEN \
        -Dsonar.login=$SONAR_TOKEN

else
     mvn org.jacoco:jacoco-maven-plugin:prepare-agent sonar:sonar -Dsonar.login=$SONAR_TOKEN
fi
#
