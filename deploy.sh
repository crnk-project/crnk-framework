#!/bin/bash
if [ "$TRAVIS_BRANCH" == "stable" -a "$TRAVIS_PULL_REQUEST" == "false" ]; then
  ./gradlew publish promote --max-workers=1 --no-parallel
else
  echo "Skip deploying";
fi