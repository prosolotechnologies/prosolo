#!/bin/bash

SILENT=$1

./bin/stop.sh rabbitmq-docker $SILENT
./bin/remove.sh $SILENT
./bin/build.sh
docker run -d -p 5672:5672 -p 15672:15672 --name rabbitmq-docker rabbitmq-docker
git submodule update
./wait-for-it-docker/wait-for-it-docker.sh rabbitmq-docker 'Server startup complete' 20
