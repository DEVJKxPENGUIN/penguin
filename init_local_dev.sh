#!/bin/bash

CLEAN=false
while (("$#")); do
  if [ "-clean" = $1 ]; then
    CLEAN=true
    echo "CLEAN = ${CLEAN}"
  fi
  shift
done

## check /docker directory
####################################################
BASE_DIR="$( cd "$( dirname "$0" )" && pwd )"
echo $BASE_DIR
DOCKER_DIR="$BASE_DIR/docker"

if [ "$CLEAN" = true ]; then
  rm -rf "$DOCKER_DIR"
fi

if [ -d "$DOCKER_DIR" ]; then
  echo "/docker directory is already exists!"
else
  echo "mkdir new /docker directory"
  mkdir "docker"
fi

MYSQL_DIR="$DOCKER_DIR/mysql"
initialize=false
if [ -d "$MYSQL_DIR" ]; then
  echo "/docker/mysql directory is already exists!"
else
  echo "mkdir new /docker/mysql directory"
  mkdir "docker/mysql"
  mkdir "docker/mysql/data"
  mkdir "docker/mysql/init"
  cp db/ddl.sql docker/mysql/init/ddl.sql
  initialize=true
fi

REDIS_DIR="$DOCKER_DIR/redis"
if [ -d "$REDIS_DIR" ]; then
  echo "/docker/redis directory is already exists!"
else
  echo "mkdir new /docker/redis directory"
  mkdir "docker/redis"
  mkdir "docker/redis/conf"
  cp db/redis.conf docker/redis/conf/redis.conf
fi
# check /docker directory finished
####################################################


## init mysql container
####################################################
DOCKER_NAME=mysql-devjk
PORT=43306
MYSQL_ROOT_PASSWORD=develop

containerExists=$(docker ps -a | grep ${DOCKER_NAME})

echo "mysql container exists : ${containerExists}"

if [ -n "${containerExists}" ]; then
  echo "${DOCKER_NAME} container is already exists, removing container..."
  docker stop ${DOCKER_NAME}
  docker rm ${DOCKER_NAME}
fi

echo "${DOCKER_NAME} : running new container..."
volumeOpt="-v `pwd`/docker/mysql/data:/var/lib/mysql"
initializeOpt=" -v `pwd`/docker/mysql/init:/docker-entrypoint-initdb.d"
if [ "$initialize" = true ]; then
  volumeOpt="$volumeOpt $initializeOpt"
fi
docker run --name $DOCKER_NAME -p $PORT:3306 $volumeOpt -e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD -d mysql:8

## init mysql container finished
####################################################

## init redis container
####################################################
DOCKER_NAME=redis-devjk
PORT=46379

containerExists=$(docker ps -a | grep ${DOCKER_NAME})

echo "redis container exists : ${containerExists}"

if [ -n "${containerExists}" ]; then
  echo "${DOCKER_NAME} container is already exists, removing container..."
  docker stop ${DOCKER_NAME}
  docker rm ${DOCKER_NAME}
fi

echo "${DOCKER_NAME} : running new container..."
volumeOpt="-v `pwd`/docker/redis/conf:/usr/local/etc/redis"
#initializeOpt=" -v `pwd`/docker/mysql/init:/docker-entrypoint-initdb.d"
initializeOpt=""
if [ "$initialize" = true ]; then
  volumeOpt="$volumeOpt $initializeOpt"
fi

docker run --name $DOCKER_NAME -p $PORT:6379 $volumeOpt -d redis:7-alpine redis-server /usr/local/etc/redis/redis.conf
