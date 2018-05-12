#!/bin/bash
if [ "$TRAVIS_BRANCH" == "stable" -a "$TRAVIS_PULL_REQUEST" == "false" ]; then
  ./gradlew publish promote
else
  echo "Skip deploying";
fi