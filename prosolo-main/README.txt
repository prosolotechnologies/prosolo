INSTRUCTIONS FOR SETTING UP THE ENVIRONMENT

- REGULAR USERS

For Eclipse:

(prerequisite is that Maven is already installed. For instructions on setting up Maven, please visit 
http://maven.apache.org/download.html#Installation)

Go to the Run/External Tools/External Tools ..." menu item on the "Run" menu bar. Select "Program" and click the 
"New" button. On the "Main" tab, fill in the "Name:" "Run ProSolo", for "Location:" as the full path to your "mvn" 
executable. For the "Working Directory:" select the workspace that matches your webapp. For "Arguments:" add 

	jetty:run

Move to the "Environment" tab and click the "New" button to add a new variable named MAVEN_OPTS with the value:

	-Xmx1024m -Xms512m

Now you can run the project on Run -> External Tools -> Run ProSolo, or from a shortcut on a main Toolbar of the 
Eclipse (the icon next to run button - green play button)

- DEVELOPERS

Go to the Run/External Tools/External Tools ..." menu item on the "Run" menu bar. Select "Program" and click the 
"New" button. On the "Main" tab, fill in the "Location:" as the full path to your "mvn" executable (e.g. /usr/bin/mvn). For the 
"Working Directory:" select the workspace that matches your webapp. For "Arguments:" add 

	jetty:run

Move to the "Environment" tab and click the "New" button to add a new variable named MAVEN_OPTS with the value:

	-Xmx1024m -Xms512m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8081,server=y,suspend=n

On some machines (Windows 7 machine) this configuration has worked instead of the first one:

	-Xmx1024m -Xms512m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_shmem,address=8081,server=y,suspend=n

If you supply suspend=y instead of suspend=n, run of the program will wait for the debugger to be invoked. This is useful 
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