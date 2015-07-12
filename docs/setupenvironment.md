[Back to previous screen](../docs/developers.md)

# Instructions for setting up the development environment

## Install Eclipse IDE for Java EE Developers
 [Download links](https://eclipse.org/downloads/)
 
## Import project from Git Repository  branch master
 Replace yourusername in [git url](https://yourusername@bitbucket.org/prosolo/prosolo-multimodule.git) and checkout master branch of Prosolo project.
 Maven module containing three submodules will be downloaded.

## Import project into Eclipse workspace as Maven project.
 Import project in your eclipse workspace in the following way:
 
 Import->Existing Maven Projects-> Browse for Prosolo-multimodule project
 
 As a result, you should have in Project Explorer prosolo-multimodule and it's three submodules: bigdata.common, prosolo and prosolo-analytics.

## Install MySQL database and create database and user that will be used by Prosolo.
[Official instructions](http://www.tutorialspoint.com/mysql/mysql-installation.htm)

## Install MongoDB database.

[Official instructions](http://docs.mongodb.org/manual/installation/)
 
## Install Cassandra database.

[Official instructions](http://www.planetcassandra.org/cassandra/?gclid=CjwKEAjwlPOsBRCWq5_e973PzTgSJACMiEp2RCMM4afybKHIeOq5u5lU_LjL1vC4pcuN28KnWPBYTRoCpjHw_wcB)


## Install Elasticsearch server version 1.6.0 (optional).

At the moment Prosolo supports Elasticsearch version 1.6.0. 


[Official instructions](https://www.elastic.co/guide/en/elasticsearch/reference/1.6/setup.html)


## Install Elasticsearch plugin mapper-attachments version 2.6.0 (required in case step 6 has been performed).

Install plugin mapper-attachments version 2.6.0 which is compatible with Elasticsearch 1.6.0. 
[Official instructions](https://github.com/elastic/elasticsearch-mapper-attachments)

## Install, configure and run RabbitMQ distributed messaging system.

There are 2 possible approaches to run RabbitMQ server
1) Download RabbitMQ docker image from [git repository](https://zjeremic@bitbucket.org/prosolo/prosolo-dockers.git)
Build and run docker image in container, and server with required settings will be up and running.

2) Download and install RabbitMQ on your local PC using [Official instructions](https://www.rabbitmq.com/download.html)
After RabbitMQ is up and running you should:
- add vhost /prosolo
- add user prosolo with password e.g. prosolo@2014
- set permissions "/prosolo prosolo  ".*" ".*" ".*""
- restart RabbitMQ server


## Compile Prosolo multimodule application.

- cd to the prosolo-multimodule
- mvn clean compile install -DskipTests=true

## Run Prosolo main application.

- from **prosolo-multimodule** cd to **prosolo-main**
- mvn jetty:run
- You will have exceptions at this point since databases and elasticsearch are not configured yet, but that is ok.
- stop the application


## Run Prosolo analytics application.

- from prosolo-multimodule cd to prosolo-analytics
- mvn jetty:run
- You will have exceptions at this point since databases and elasticsearch are not configured yet, but that is ok.
- stop the application 

## Configure generated configuration files for both applications.

- in your home directory new directory will be created ".prosolo" containing three xml files used for configuration
- **prosolo_common_config.xml** - shared configuration file for both applications
	- namespace if any resource is used on shared server (e.g. rabbitmq, cassandra or elasticsearch), each developer has to have his own unique namespace (e.g. Nikola -> N, Sanja -> S, Zoran -> Z). This value if other than local will change cassandra database name, elasticsearch indexes, rabbitmq queue and exchange, so it prevents conflicts.
	- contains configuration for rabbitmq server (update host, virtualHost, username and password). For shared server use host: **52.2.214.133** 
	- configuration for elasticsearch (update type to "server", host. Other settings are not important on local machine). For shared server use host: **52.2.214.133**
	- configuration for mysql server (update host, database, user and password)
	- configuration for hibernate (nothing to configure here)
- **prosolo_main_config.xml** - configuration file for prosolo-main application
	- formatDB - **true** deletes mysql, mongodb and elasticsearch and initialize new data. Use this first time and then set it to **false**
	- default-user - user with admin privileges in Prosolo
	- mongo-db-config - configuration for mongodb database. 
	- app-config (developer-email should be changed. Other settings could stay as default)
	- email-notifier/activated should be false to prevent application from sending emails
	- analytical-server (if analytical server runs per default settings, it can stay as default)
- **prosolo_analytical_config.xml**	- configuration file for prosolo-analytical application
	- init/formatDB **true** will format cassandra database
	- init/formatES **true** will format elasticsearch indexes
	- dbconfig - configuration of cassandra database.  For shared server use db-host: **52.2.214.133**
	- scheduler-config/quartz-jobs/job/activated **true** to activate specific job to run based on its schedule
	- scheduler-config/quartz-jobs/job/on-startup **true** to activate specific job at application startup. Used only for testing purposes.

- **prosolo_analytics_config.xml** - configuration file for prosolo-analytics application