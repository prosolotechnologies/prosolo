#!/bin/bash
USER=${RABBITMQ_USERNAME:-rabbitmq}
PASS=${RABBITMQ_PASSWORD:-$(pwgen -s -1 16)}

# Create User
echo "Creating user: \"$USER\"..."
cat > /etc/rabbitmq/rabbitmq.config <<EOF
[
{rabbit, [{default_user, <<"$USER">>},{default_pass, <<"$PASS">>},{tcp_listeners, [{"0.0.0.0", 5672}]}]}
].
EOF

echo "========================================================================"
echo "RabbitMQ User: \"$USER\""
echo "RabbitMQ Password: \"$PASS\""
echo "========================================================================"

rm -f /.firstrun