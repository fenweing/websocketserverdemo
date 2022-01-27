#!/bin/bash
artifactId=$1
version=$2
if [[ -z $1 || -z $2 ]];then
	echo 'artifactId and version cannot be empty!'
	exit
fi
basePath=/root/project/code
cd $basePath/$artifactId
mvn clean package
jarName=$artifactId-$version
cstop ${jarName}.jar
nohup java -jar ${jar.name}.jar 2>&1 >> $basePath/$artifactId/nohup &

