#!/bin/bash

DEBUG=false
CLEAN=""
RUN_MODULE=""
RUN_DIRECTORY=""
MODE="local"
RUN_TYPE="spring" ## spring or npm
GRADLE_TASK="bootRun"
DB_HOST="localhost:43306"
DB_USERNAME="root"
DB_PASSWORD="develop"
REDIS_HOST="localhost"
REDIS_PORT="46379"
REDIS_PASSWORD="develop"
JWT_KEY="0759fb6999488bbcbc79a827dc4be237bc796498f1d9bc091f6ad4d4abf4494a
a2f15f4a9d445870e8ac1adefc5958f8610e52b581791867f0f6c29045d99ffd
33b54dc2b984f3083a220863c1f78975cbc67fcc62f2192b5cc3e8b2a62ab95d
70f4250aaa3bf3ab9b9feb35e52d2e9b23fc36cbfd0e2e7271107774c9de84ea
cb5338fe58c08d478445e74745012bf256011241924d2b61681284e47c89bb61
995f407f806b20856b843963798491adf5d2d9e61e56b4bd6ad3b8cdfa5cb843
c7d7956168fcf2d0c994d3eefdc445624ddac0775ae897b6256359e5e1a4925f
d6247f71e3716c46e5121c7f1aae5119015aa980131f41c05b05c475b3a37a59
38889f814fec42f2c2bd3fc6280e4d32194151486447d532d6f972e9bf0bc015
93aea368da3555430c84f73374c7422d7b2fe037cef4433af0955cc64f4d28b3"

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
  if [ "-test" = $1 ]; then
    GRADLE_TASK=test
  fi
  shift
done

if [ ${RUN_TYPE} = "spring" ]; then

  # process run options
  ARGS="--stacktrace --debug --quiet"
  if [ "${GRADLE_TASK}" = "test" ]; then
    ARGS=""
  fi
  if [ ${DEBUG} = true ]; then
    ARGS="${ARGS} --debug-jvm"
  fi
  if [ -z ${RUN_MODULE} ]; then
    echo 'you should choose module option (-web, -auth)'
    exit 1
  fi

  # make full args
  # shellcheck disable=SC2089
  FULL_ARGS="./gradlew ${CLEAN} $RUN_MODULE:$GRADLE_TASK ${ARGS} --args='--spring.profiles.active=${MODE}'"
  if [ "${GRADLE_TASK}" = "test" ]; then
    FULL_ARGS="./gradlew ${CLEAN} $RUN_MODULE:$GRADLE_TASK ${ARGS}"
  fi

  echo RUN_MODULE : ${RUN_MODULE}
  echo ARGS : "${ARGS}"
  echo MODE : ${MODE}
  echo FULL_ARGS : "${FULL_ARGS}"

  # gradle run
  # shellcheck disable=SC2090
  export DB_HOST=${DB_HOST}
  export DB_USERNAME=${DB_USERNAME}
  export DB_PASSWORD=${DB_PASSWORD}
  export REDIS_HOST=${REDIS_HOST}
  export REDIS_PORT=${REDIS_PORT}
  export REDIS_PASSWORD=${REDIS_PASSWORD}
  export JWT_KEY=${JWT_KEY}
  ${FULL_ARGS}
  #./gradlew ${RUN_MODULE}:bootRun ${ARGS}
fi
