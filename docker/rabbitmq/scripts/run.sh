#!/bin/bash
# Initialize first run
if [[ -e /.firstrun ]]; then
/scripts/first_run.sh
fi
# Start RabbitMQ
echo "Starting RabbitMQ..."
/usr/sbin/rabbitmq-server