# Adding configuration
* Before running scripts configuration should be added for namespace in scripts/config.properties

# Creating migration scripts
* All migration scripts should be stored in scripts/sql/migration. 
* Name of the migration script should be in format date+order number, e.g. 2017083001

# Setting up database configuration
* Database configuration should be created only once for each namespace used on the server using CLI command:

mysql_config_editor set --login-path=local --host=localhost --user=user --password

# Running the script
* script update_migrate.sh performs the following actions:

    * update the code from the git repository
    * create new document version.txt with updated information in webapp root
    * create a database dump and stores it to backup directory with current timestamp
    * run all the scripts with the timestamp later than the previous timestamp stored in migration table
    * compiles the project and stores prosolo-main.war and prosolo-analytics.war in backup directory with current timestamp
    * uploads prosolo.war to tomcat/webapps directory
    
* script update_migrate.sh receives two parameters:
    * namespace - first, required parameter is used to pickup parameters from config.properties and mysql config parameters
    * timestamp - second, optional parameter is used to setup latest timestamp. In this case all earlier migration scripts will be ignored.