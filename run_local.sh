#!/bin/bash

DEBUG=false
CLEAN=""
RUN_MODULE=""
RUN_DIRECTORY=""
MODE="local"
RUN_TYPE="spring" ## spring or npm
DB_HOST="localhost:43306"
DB_USERNAME="root"
DB_PASSWORD="develop"
REDIS_HOST="localhost"
REDIS_PORT="46379"
REDIS_PASSWORD="develop"

# parse arguments
while (("$#")); do
  if [ "-debug" = $1 ]; then
    DEBUG=true
    echo "DEBUG = ${DEBUG}"
  fi
  if [ "-clean" = $1 ]; then
    CLEAN=clean
    echo "CLEAN = true"
  fi
  if [ "-web" = $1 ]; then
    RUN_MODULE=penguin-web
  fi
  if [ "-auth" = $1 ]; then
    RUN_MODULE=penguin-auth
  fi
  if [ "-local" = $1 ]; then
    MODE=local
  fi
  if [ "-dev" = $1 ]; then
    MODE=dev
  fi
  shift
done

if [ ${RUN_TYPE} = "spring" ]; then

  # process run options
  ARGS="--stacktrace --debug --quiet"
  if [ ${DEBUG} = true ]; then
    ARGS="${ARGS} --debug-jvm"
  fi
  if [ -z ${RUN_MODULE} ]; then
    echo 'you should choose module option (-web, -auth)'
    exit 1
  fi

  # make full args
  # shellcheck disable=SC2089
  FULL_ARGS="./gradlew ${CLEAN} $RUN_MODULE:bootRun ${ARGS} --args='--spring.profiles.active=${MODE}'"

  echo RUN_MODULE : ${RUN_MODULE}
  echo ARGS : "${ARGS}"
  echo MODE : ${MODE}
  echo FULL_ARGS : "${FULL_ARGS}"

  # bootRun
  # shellcheck disable=SC2090
  export DB_HOST=${DB_HOST}
  export DB_USERNAME=${DB_USERNAME}
  export DB_PASSWORD=${DB_PASSWORD}
  export REDIS_HOST=${REDIS_HOST}
  export REDIS_PORT=${REDIS_PORT}
  export REDIS_PASSWORD=${REDIS_PASSWORD}
  ${FULL_ARGS}
  #./gradlew ${RUN_MODULE}:bootRun ${ARGS}
fi
