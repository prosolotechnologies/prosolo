FROM ubuntu:latest
MAINTAINER Zoran Jeremic<zoran.jeremic@gmail.com>


ENV DEBIAN_FRONTEND noninteractive
ENV HOSTNAME rabbitmq

RUN apt-get update && apt-get dist-upgrade -y && apt-get install -y gnupg

# Make sure that Upstart won't try to start RabbitMQ after apt-get installs it
# https://github.com/dotcloud/docker/issues/446
ADD policy-rc.d /usr/sbin/policy-rc.d
RUN /bin/chmod 755 /usr/sbin/policy-rc.d

ADD rabbitmq-signing-key-public.asc /tmp/rabbitmq-signing-key-public.asc
RUN apt-key add /tmp/rabbitmq-signing-key-public.asc

RUN echo "deb http://www.rabbitmq.com/debian/ testing main" > /etc/apt/sources.list.d/rabbitmq.list
RUN apt-get -qq update > /dev/null
RUN apt-get -qq -y install rabbitmq-server > /dev/null

#RUN echo "ulimit -n 102400" >> /etc/default/rabbitmq-server
RUN /usr/sbin/rabbitmq-plugins enable rabbitmq_management
#ADD rabbitmqadmin /usr/local/rabbitmqadmin
#RUN echo "[{rabbit, [{loopback_users, []}]}]." > /etc/rabbitmq/rabbitmq.config


# Add scripts
ADD scripts /scripts
RUN chmod +x /scripts/*.sh
RUN touch /.firstrun

# Command to run
ENTRYPOINT ["/scripts/run.sh"]
CMD [""]

EXPOSE 5672 15672 4369

ADD rabbitmq_setup.sh /tmp/rabbitmq_setup.sh
ENTRYPOINT /bin/bash /tmp/rabbitmq_setup.sh

#RUN /etc/init.d/rabbitmq-server start ; rabbitmqctl add_vhost /prosolo;  rabbitmqctl add_user prosolo prosolo2014; set_permissions -p /prosolo prosolo ".*" ".*" ".*";/etc/init.d/rabbitmq-server stop
