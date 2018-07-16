#!/bin/bash

/usr/sbin/rabbitmq-server

echo "Enabling rabbitmq_management ..."
/usr/sbin/rabbitmq-plugins enable rabbitmq_management

# Start rabbitmq server
echo "Starting rabbitmq server ..."
service rabbitmq-server start

echo "Adding vhost '/prosolo' to rabbitmq ...."
/usr/sbin/rabbitmqctl add_vhost /prosolo

echo "Removing 'guest' user and adding 'admin' and 'prosolo' users to rabbitmq ..."
/usr/sbin/rabbitmqctl add_user admin d1ff1cult@123
/usr/sbin/rabbitmqctl add_user prosolo prosolo@2014
/usr/sbin/rabbitmqctl set_user_tags admin administrator
/usr/sbin/rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
/usr/sbin/rabbitmqctl set_permissions -p /prosolo prosolo ".*" ".*" ".*"
/usr/sbin/rabbitmqctl delete_user guest

 

# Stop rabbitmq server
echo "Stopping rabbitmq server ..."
service rabbitmq-server stop

# Starting rabbitmq server after adding admin user
echo "Restarting rabbitmq server after adding admin user..."
/usr/sbin/rabbitmq-server
