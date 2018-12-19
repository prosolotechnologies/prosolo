#!/usr/bin/env bash


  echo "This scripts dumps all the content of the provided keyspace from a"
  echo "local cassandra docker container int csv files located in ~/cassandra_backup. "
  echo "It requires cqlsh to be available on system PATH"
  echo "USAGE: ./backup_docker_cassandra.sh keyspace"

BACKUP_DIR=~/cassandra_backup
mkdir -p $BACKUP_DIR

  cqlsh -e "use $1; describe keyspace" >> ${BACKUP_DIR}/$1_schema.cql
  for table in $(cqlsh -e "desc $1" | grep 'CREATE TABLE' | awk '{print $3}');
  do
    echo 'copying table '$table;
    cqlsh -k $1 -e "copy ${table} to stdout" > ${BACKUP_DIR}/${table}.csv;
  done







