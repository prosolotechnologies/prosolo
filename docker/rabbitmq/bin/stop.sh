#!/bin/bash
# stop-docker-container.sh <CONTAINER> [--silent]

CONTAINER="$1"

SILENT=false

if [[ "$2" == "--silent" ]]; then
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

if docker ps | grep "$CONTAINER" &>/dev/null; then
  log "Stopping docker container $CONTAINER"
  run_command "docker stop $(docker ps | grep "$CONTAINER" | awk '{print $1}')"
  exit 0
else
  log "Could not find running docker container $CONTAINER"
  if docker ps -a | grep "$CONTAINER" &>/dev/null; then
    log "Container $CONTAINER exists but not running"
  fi
  exit 1
fi
