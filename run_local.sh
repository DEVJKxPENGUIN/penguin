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
JWT_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDFqfSszJKub4Rf
Ge35GDJO5IMc8l3Rk9BHtFLg3t8f5x4EqKUe5ihkgcYi03oPY1dBkC8JRT8UI1bU
U8xDUELZZNKJXflzznkb5Q4Lc7aIjo8Ic42rgDUXgP9UYUK6oqBoD2PPvb3Wj41X
s1+XGceA3bh8SLhQiqrdyqNd4+SDWcaYVjK1khWXFxcn8HRFpf1TAOfgiwWHx8Hw
cEvmp4WSflUGZEsdKb5TNMvWzCA2wS0iOvlCTKw/Dioj73AJkdTlfMn7pKqaFyGy
6gMdMBa7y5IYk8d0VX8usGUfnRBlqaiPJkekUnHwm1sO/9hmV21xVCMzWx8IBhW6
iEhzqKc1AgMBAAECggEAATZjq9OtaqiAdVzhWs/NLzKzoy/RZ4Gym139zCdR0w5F
3t1x9diPkt5A6uHFM+b2GA7cWwAzvNnipHbHH/qUDmVR7UnIR++SSpuDFIrnKbtG
IVUkH1MwAC7oYgoesKpitYCyqgmN6o0C3vDYu0KLOL0jbo+7obLYDXg6vE6zjiKy
FKm32F0pP/wbg+8KgxXW74wdFbFJlknnrWzKHmhzWIkJmNdju0oHiIW2QdDnmUv8
lbDnYjoFGs6X1EisrNmiy2HltMTYWTHEkDrhDVNfLIjgdVf0Hn25v85DM+MVcwNT
WFrXrkU1rFrgecwpftgfniyOGDQso4sGrEnRrMMt2QKBgQDijtPwONJXFVoCgO8O
+LnWm/c3II2XMwT1cCckv1+Di9v1/bIn1Wd6ZKAK1jFKjcKw1rFr1R8Z4LlGZqCn
hBFe4j9Z2ZO9IE2NA1Ds94iIjAqC2SD/OCFOFIqdKQkEy6Sf/Or/IQljoKiaZZiN
U15Hjmz4cGwtR4rkdVhtjY2aVwKBgQDfWeAvVKAmNwDmxn3tf6TQf5p7YYES5kdH
Mkv9onYD8gMVPY9ih5e2NRyJQ14Sqogc+lm+YNJLyshii1C4RFk0aLClM4vCJadY
051dVw5mw3aOeH/cVIB05zHgESRja6zswTyIwmZipjRz0HrZFH1PugliUWitJ9vZ
04U2OuUrUwKBgQCkjdsQQyU7zS1YXi/ErxHD9/qClulgpdT3NCOqRDnqwP3d2Qu6
dP0437eCx3p7zSVY7kdlrCFv2VKY+Ne9dGZthWVALJFrL/hD8OGpjE0l6JmF2pFl
7m36WV8osXJ0gjkrXzeMYYGBVGLB6gL4u1HiJWeut3FLHgia0USOJd9w9wKBgFyf
fvLh21A9uyf45LwbFRRJ9px4APj/ekuyNgVG3D4inNiwIMgxht7PA7iAsYB4K94o
jBMBP/SicZs8PwtLfNrZF3hK+ghXaSDTEQ1mTtia8o1+tzd964+PWQWc1kRMoPfF
4v6r7521Y0csC+X+M0Fe2wlkzU4Rm4Cn0TR+FA91AoGBAIcShCEVGhvqIR2tPQFx
xTjxfbx3UMnW8ahCcE2cmL1wDNOjIn1mP75Ii15/mrfsU6nHko/Xmx6SGtYiU+OF
r91i3TnTD9AjfrH0Ts9O+/PYhjY3zYkjd8l0BU1jfb4Jgjd9+oprkAYqTxeke+2I
Dyb5bPWCYdtvv6pvhuOLYwUg
-----END PRIVATE KEY-----"
JWT_PUBLIC_KEY="-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxan0rMySrm+EXxnt+Rgy
TuSDHPJd0ZPQR7RS4N7fH+ceBKilHuYoZIHGItN6D2NXQZAvCUU/FCNW1FPMQ1BC
2WTSiV35c855G+UOC3O2iI6PCHONq4A1F4D/VGFCuqKgaA9jz7291o+NV7NflxnH
gN24fEi4UIqq3cqjXePkg1nGmFYytZIVlxcXJ/B0RaX9UwDn4IsFh8fB8HBL5qeF
kn5VBmRLHSm+UzTL1swgNsEtIjr5QkysPw4qI+9wCZHU5XzJ+6SqmhchsuoDHTAW
u8uSGJPHdFV/LrBlH50QZamojyZHpFJx8JtbDv/YZldtcVQjM1sfCAYVuohIc6in
NQIDAQAB
-----END PUBLIC KEY-----"

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
  if [ "-penguin-web" = $1 ]; then
    RUN_MODULE=penguin-web
  fi
  if [ "-auth" = $1 ]; then
    RUN_MODULE=penguin-auth
  fi
  if [ "-penguin-auth" = $1 ]; then
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
  export JWT_PRIVATE_KEY=${JWT_PRIVATE_KEY}
  export JWT_PUBLIC_KEY=${JWT_PUBLIC_KEY}
  ${FULL_ARGS}
  #./gradlew ${RUN_MODULE}:bootRun ${ARGS}
fi
