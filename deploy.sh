#!/bin/bash
if [ "$TRAVIS_BRANCH" == "master" -a "$TRAVIS_PULL_REQUEST" == "false" ]; then
  ./gradlew publish --max-workers=1 --no-parallel
else
  echo "Skip deploying";
fi