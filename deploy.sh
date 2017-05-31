#!/bin/bash

echo "Branch : $TRAVIS_BRANCH";
echo "PR Branch : $TRAVIS_PULL_REQUEST_BRANCH";

if [ "$TRAVIS_BRANCH" == "master" ]; then
  ./gradlew publish
fi