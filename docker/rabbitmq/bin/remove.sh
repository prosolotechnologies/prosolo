#!/bin/bash

./bin/stop.sh "$1"

SILENT=false

if [[ "$1" == "--silent" ]]; then
  SILENT=true
fi

log() {
  if ! $SILENT; then
    echo $1
  fi
}

run_command() {
  if $SILENT; then
    $1 &>/dev/null
  else
    $1
  fi
}

if docker ps -a | grep rabbitmq-docker 2>/dev/null >&2; then
  log "Removing docker container rabbitmq-docker"
  run_command "docker rm $(docker ps -a | grep rabbitmq-docker | awk '{print $1}')"
fi

if docker images -f "dangling=true" -q 2>/dev/null >&2; then
  log "Removing docker images that are unused (dangling)"
  run_command "docker image prune -f"
fi

if docker images | grep rabbitmq-docker 2>/dev/null >&2; then
  log "Removing docker image rabbitmq-docker"
  run_command "docker image rm $(docker images | grep rabbitmq-docker | awk '{print $1}')"
else
  log "Could not find docker image rabbitmq-docker"
fi
