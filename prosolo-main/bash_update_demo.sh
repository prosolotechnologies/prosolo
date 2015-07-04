#!/bin/bash
#run on demo server as ubuntu user
svn update
echo $1
PROSOLO_HOME=/home/ubuntu/prosolo-development/development/prosolo
rm $PROSOLO_HOME/src/main/resources/config/config.xml
cp $PROSOLO_HOME/src/main/resources/config/config_demo.xml $PROSOLO_HOME/src/main/resources/config/config.xml 
if [ "$1" = "debug" ];then
	echo "DEBUG IS TURNED ONs"
rm $PROSOLO_HOME/src/main/resources/config/log4j.properties
cp $PROSOLO_HOME/src/main/resources/config/log4j_debug.properties $PROSOLO_HOME/src/main/resources/config/log4j.properties
fi
#rm $PROSOLO_HOME/src/main/webapp/WEB-INF/web.xml
#cp $PROSOLO_HOME/src/main/webapp/WEB-INF/web_production.xml $PROSOLO_HOME/src/main/webapp/WEB-INF/web.xml
#rm $PROSOLO_HOME/src/main/resources/config/log4j.properties
#cp $PROSOLO_HOME/src/main/resources/config/log4j_production.properties $PROSOLO_HOME/src/main/resources/config/log4j.properties
mvn clean compile
mvn package -DskipTests
