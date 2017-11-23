#!/bin/bash
if [ "$TRAVIS_BRANCH" == "master" -a "$TRAVIS_PULL_REQUEST" == "false" ]; then
  ./gradlew publish --max-workers=1
else
  echo "Skip deploying";
fi