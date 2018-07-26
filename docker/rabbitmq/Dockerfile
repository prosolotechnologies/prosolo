FROM rabbitmq:3.7.3-management

WORKDIR /

COPY ./rabbitmq/rabbitmq.conf ./etc/rabbitmq/rabbitmq.conf
COPY ./rabbitmq/definitions.json ./etc/rabbitmq/definitions.json

RUN rabbitmq-plugins enable rabbitmq_management
