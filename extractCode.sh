#!/bin/bash
artifactId=$1
version=$2
if [[ -z $1 || -z $2 ]];then
	echo 'artifactId and version cannot be empty!'
	exit
fi
basePath=/root/project/code
cd $basePath
rm -fr ${baePath}/$artifactId
mkdir -p ${basePath}/$artifactId
jarName=$artifactId-$version
unzip -o -qq -d $artifactId ${jarName}.zip
rm -fr $artifactId/.git
