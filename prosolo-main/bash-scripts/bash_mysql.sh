#!/bin/bash -x

BASENAME=/home/ubuntu/backups
mysqldump  prosolo -u prosolo  > $BASENAME/prosolo_db_dump.sql


DATE=$(date +%Y%m%d_%H%M%S)
DBARCHIVE=$BASENAME/MYSQL_$DATE.tar.gz

tar  -czvf $DBARCHIVE $BASENAME/prosolo_db_dump.sql
rm $BASENAME/prosolo_db_dump.sql
echo "All archived in $DBARCHIVE"