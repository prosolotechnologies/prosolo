[Back to previous](../developers.md)

# Instructions for setting up the development environment

## Install Eclipse IDE for Java EE Developers
  https://eclipse.org/downloads/
 
## Import project from Git Repository  branch master
 Replace yourusername in url https://yourusername@bitbucket.org/prosolo/prosolo-multimodule.git and checkout master branch of Prosolo project.
 Maven module containing three submodules will be downloaded.

## Import project into Eclipse workspace as Maven project.
 Import project in your eclipse workspace in the following way:
 
 Import->Existing Maven Projects-> Browse for Prosolo-multimodule project
 
 As a result, you should have in Project Explorer prosolo-multimodule and it's three submodules: bigdata.common, prosolo and prosolo-analytics.

## Install MySQL database and create database and user that will be used by Prosolo.
http://www.tutorialspoint.com/mysql/mysql-installation.htm

## Install MongoDB database.

http://docs.mongodb.org/manual/installation/

## Install Cassandra database.

http://www.planetcassandra.org/cassandra/?gclid=CjwKEAjwlPOsBRCWq5_e973PzTgSJACMiEp2RCMM4afybKHIeOq5u5lU_LjL1vC4pcuN28KnWPBYTRoCpjHw_wcB


## Install Elasticsearch server version 1.4.4 (optional).

At the moment Prosolo supports Elasticsearch version 1.4.4. It could be upgraded to version 1.6

Download version 1.4.4. from past releases https://www.elastic.co/downloads/past-releases?page=2
Follow instructions to install it https://www.elastic.co/guide/en/elasticsearch/reference/1.6/setup.html


## Install Elasticsearch plugin mapper-attachments version 2.4.3 (required in case step 6 has been performed).

Install plugin mapper-attachments version 2.4.3 which is compatible with Elasticsearch 1.4.4. Elasticsearch 1.6 will require plugin version 2.6.0
https://github.com/elastic/elasticsearch-mapper-attachments

## Install, configure and run RabbitMQ distributed messaging system.

There are 2 possible approaches to run RabbitMQ server
1) Download RabbitMQ docker image from git repository https://zjeremic@bitbucket.org/prosolo/prosolo-dockers.git
Build and run docker image in container, and server with required settings will be up and running.

2) Download and install RabbitMQ on your local PC using official instructions at https://www.rabbitmq.com/download.html
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
	- contains configuration for rabbitmq server (update host, virtualHost, username and password) 
	- configuration for elasticsearch (update type to "server", host. Other settings are not important on local machine) 
	- configuration for mysql server (update host, database, user and password)
	- configuration for hibernate (nothing to configure here)
- **prosolo_main_config.xml** - configuration file for prosolo-main application
- **prosolo_analytics_config.xml** - configuration file for prosolo-analytics application