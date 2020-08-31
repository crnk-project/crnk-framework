#!/bin/bash
set - e
if [ "$TRAVIS_BRANCH" == "stable" -a "$TRAVIS_PULL_REQUEST" == "false" ]; then
  ./gradlew publish promote --max-workers=1 --no-parallel -Pstable=true
elif [ "$TRAVIS_BRANCH" == "master" -a "$TRAVIS_PULL_REQUEST" == "false" ]; then
  ./gradlew publish --max-workers=1 --no-parallel
else
  echo "Skip deploying";
fi