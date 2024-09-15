#!/bin/zsh


DB_HOST="localhost:43306"
DB_USERNAME="root"
DB_PASSWORD="develop"
REDIS_HOST="localhost"
REDIS_PORT="46379"
REDIS_PASSWORD="develop"

export DB_HOST=${DB_HOST}
export DB_USERNAME=${DB_USERNAME}
export DB_PASSWORD=${DB_PASSWORD}
export REDIS_HOST=${REDIS_HOST}
export REDIS_PORT=${REDIS_PORT}
export REDIS_PASSWORD=${REDIS_PASSWORD}

./gradlew penguin-auth:bootRun --args='--spring.profiles.active=local' --debug
