#!/bin/bash -x

BASENAME=/home/ubuntu/backups
DATE=$(date +%Y%m%d_%H%M%S)
DUMPDIR=$BASENAME/MONGODB_$DATE
DBARCHIVE=$BASENAME/MONGODB_$DATE.tar.gz

mongodump --db prosolo -o $DUMPDIR
tar  -czvf $DBARCHIVE $DUMPDIR 
rm -r $DUMPDIR

#to restore
#mongorestore --db prosolo log_user_latest_activity_time.bson
