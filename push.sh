#!/bin/bash
artifactId=$1
version=$2
if [[ -z $1 || -z $2 ]];then
	echo 'artifactId and version cannot be empty!'
	exit
fi
basePath=/root/project/code/$artifactId
cd $basePath
if [[ ! -d .git ]];then
    git clone https://github.com/fenweing/${artifactId}.git
    mv ${artifactId}/.git ./
    rm -fr ${artifactId}
fi
git add .
git commit -m "commit"
git push origin
