#!/bin/bash
mysqldump  prosolo -u prosolo -p > /home/ubuntu/prosolo-new/prosolo_db_dump.sql
#mysql prosolo -u prosolo -p < prosolo_db_dump.sql

cp -R /home/ubuntu/upload/ /home/ubuntu/prosolo-new/backups/upload

BASENAME=/home/ubuntu/prosolo-new/backups/
DATE=$(date +%Y%m%d_%H%M%S)
DBARCHIVE=$BASENAME/DB_$DATE.tar.gz
UPLOADARCHIVE=$BASENAME/DB_$DATE.tar.gz

tar  -czvf $DBARCHIVE /home/ubuntu/prosolo-new/backups/prosolo_db_dump.sql
tar  -czvf $UPLOADARCHIVE /home/ubuntu/prosolo-new/backups/upload
echo "All archived in $DBARCHIVE"
