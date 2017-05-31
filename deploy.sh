#!/bin/bash

echo "Branch : $TRAVIS_BRANCH";

if [ "$TRAVIS_BRANCH" == "master" ]; then
  ./gradlew publish
fi