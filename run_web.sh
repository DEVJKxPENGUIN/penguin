#!/bin/zsh

./gradlew penguin-web:bootRun --args='--spring.profiles.active=local' --debug
