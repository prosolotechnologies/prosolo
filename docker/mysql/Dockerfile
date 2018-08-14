FROM mysql:5.7

ADD init_scripts/init_db.sql /docker-entrypoint-initdb.d/init_db.sql

EXPOSE 3306
CMD ["mysqld"]
