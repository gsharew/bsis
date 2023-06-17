#!/bin/bash
#mvn tomcat7:run
set -eu

sudo docker-compose up -d mysql
mvn clean package
#sudo docker run -d -p 8080:8080 -v /home/gsharew/bsis/src/main/java/:/usr/local/tomcat/webapps --name ce4ed86ac5841 bsis_bsis
sudo docker-compose up bsis
