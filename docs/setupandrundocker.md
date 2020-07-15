Before running Docker images, Docker and Docker Compose need to be instaled.

Install Docker
OS X
Get Docker for Mac
Docker Compose is already included in the Docker for Mac, so Mac users do not need to install Compose separately.
Install Docker on Windows
Get Docker for Windows
Docker Compose is already included in the Docker for Windows, so Mac users do not need to install Compose separately.
Install Docker on Linux
Follow the instructions from the link, which involve running the curl command in your terminal to download the binaries.

Install the DOcker by following the instructions. 
Run this command to download the latest version of Docker Compose:

sudo curl -L https://github.com/docker/compose/releases/download/1.22.0/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
Use the latest Compose release number in the download command.

The above command is an example, and it may become out-of-date. To ensure you have the latest version, check the Compose repository release page on GitHub.
Apply executable permissions to the binary:

sudo chmod +x /usr/local/bin/docker-compose
Optionally, install command completion for the bash and zsh shell.
Test the installation.

```$ docker-compose --version```

```docker-compose version 1.22.0, build 1719ceb```

Initialize Docker Images
Navigate to the project root (the root of the prosolo-multimodule project) and run the initdocker.sh script from the Terminal. 

On Linux and OS X: 

```./initdockers.sh```


This script can accept different parameters

To get help on using this script use one of the following two commands:

```./initdockers.sh -h```
 
or
 
```./initdockers.sh --help```


To initialise elasticsearch docker container with Elasticsearch version 6.2.4 use parameter -es, e.g.:

```./initdockers.sh -es6```
If you don't use this parameter Elasticsearch version 2.3.0 will be initialised as default version.

Docker containers volumes
Docker containers for cassandra, elasticsearch and mysql are initialised with volumes used to store data which are located on local machine. These script makes it possible to use different volumes for different work you are performing. For example, you might want to initialize your data for testing bug related to the specific business case, and you find BC5 most convenient for that task. However, for your regular development, you might prefer BC4. This script makes it possible to initalize different volumes when you start containers. 

Default configuration is to initialise volumes whose name is the same as the git branch name you are currently working on.

Another configuration is meant to be used for everyday development, and volume name for container is 'dev'. It can be initialised using one of the following two commands:

```./initdockers.sh -d```
 
or
 
```./initdockers.sh --dev```


Eventually you could decide to give your custom names of the volumes, using argument -b, e.g.:



```./initdockers.sh -b BC5```
 
or
 
```./initdockers.sh -b testing```


Other parameters for initialisation of dockers
reset - resets database content and repeats bootstrap process
stop - stops the currently running databases
debug - prevents running containers in the background (default value). This option is to be used to investigate docker container initialisation problems as it will output logs from the container in terminal. Ctrl+C or closing terminal stops containers


Troubleshooting:


Elasticsearch 6.2.3 won't start because of the following exception:

vm.max_map_count [65530] likely too low, increase to at least [262144]

Configure vm.max_map_count for your OS system as described here 