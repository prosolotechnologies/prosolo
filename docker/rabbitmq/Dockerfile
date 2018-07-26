FROM rabbitmq:3.7.3-management

WORKDIR /

COPY rabbitmq.conf ./etc/rabbitmq/rabbitmq.conf
COPY definitions.json ./etc/rabbitmq/definitions.json

RUN rabbitmq-plugins enable rabbitmq_management
