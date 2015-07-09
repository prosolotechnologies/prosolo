[Back to main](../README.md)

# Instructions for setting up the environment for development

## Setting up development environment

1. Install Eclipse IDE for Java EE Developers
2. Import project from Git Repository https://yourusername@bitbucket.org/prosolo/prosolo-multimodule.git branch develop
3. Import project into Eclipse workspace as Maven project.
4. Install MySQL database and create database and user that will be used by Prosolo.
5. Install MongoDB database.
6. Install Cassandra database.
7. Install Elasticsearch server version 1.4.4 (optional).
8. Install Elasticsearch plugin mapper-attachments version 2.4.3 (required in case step 6 has been performed).
9. Install, configure and run RabbitMQ distributed messaging system.
10. Compile Prosolo multimodule application.
11. Run Prosolo main application.
12. Run Prosolo analytics application.
12. Configure generated configuration files for both applications.


## Running/debuging application in Eclipse 

Go to the Run/External Tools/External Tools ..." menu item on the "Run" menu bar. Select "Program" and click the 
"New" button. On the "Main" tab, fill in the "Location:" as the full path to your "mvn" executable (e.g. /usr/bin/mvn). For the 
"Working Directory:" select the workspace that matches your webapp. For "Arguments:" add 

	jetty:run

Move to the "Environment" tab and click the "New" button to add a new variable named MAVEN_OPTS with the value:

	-Xmx1024m -Xms512m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8081,server=y,suspend=n

On some machines (Windows 7 machine) this configuration has worked instead of the first one:

	-Xmx1024m -Xms512m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_shmem,address=8081,server=y,suspend=n

If you supply suspend=y instead of suspend=n, run of the program will wait for the debugger to be invoked. This is usefull 
when debugging application startup process.

--Step 2--

Then, pull up the "Run/Debug Configurations..." menu item and select "Remote Java Application" and click the 
"New" button. Fill in the dialog by selecting your webapp project for the "Project:" field, and ensure you are 
using the same port number as you specified in the address= property above (port 8081).

Now all you need to do is to Run/External Tools and select the name of the maven tool setup you created in step 1 
to start the plugin and then Run/Debug and select the name of the debug setup you setup in step2.

--Stopping Jetty--

In order to stop the jetty server the "Allow termination of remote VM" should be checked in debug dialog in Step 2. 
When you have the jetty server running and the debugger connected you can switch to the debug perspective. In 
the debug view, right click on the Java HotSpot(TM) Client VM[localhost:8081] and  chose terminate. This will stop 
the debugger and the jetty server.

## Configuration file

Project use typesafe for configuration files. This means there is a pseudo-json file "development.conf" that contains configurations for each database.
This file could be provided at deployment time as an environment-specific config file, which will override default one. 

## Services
- REST API services. All classes implementing these services are in package com.warrantylife.api. 
  These services are responsible for providing the following functionalities:

	- receiving user interactions 
	- interactions with front-end dashboard application 
	- recommendations for customer applications
	
## REST API

	GET http://localhost:8080/api/recommendation/features/{productid}/{warrantyid}/{locationid}/{sessionid}
	
	- not implemented yet
	
	
	GET http://localhost:8080/api/recommendation/features/{warrantyid}/{locationid}/{sessionid}
	
	- not implemented yet
	
	GET http://localhost:8080/api/recommendation/warranty/{product}/{locationid}/{sessionid}
	
	...response example here...
	
	
	POST http://localhost:8080/api/interaction/logs
	
	Json input:
	...example here...
	
	GET http://localhost:8080/generator/categories/list
	
	GET http://localhost:8080/generator/category/product/list/{categoryid}
	
	GET http://localhost:8080/generator/category/warrantyprofiles/list/{categoryid}
	
	GET http://localhost:8080/generator/category/product/generatelogs
	
	Json input:
	...example here...
	
	