#!/bin/bash
artifactId=$1
version=$2
if [[ -z $1 || -z $2 ]];then
	echo 'artifactId and version cannot be empty!'
	exit
fi
jarName=$1-$2
echo '' > /root/project/wagon.log
cd /root/project/$artifactId
pwd
if [ -e ${jarName}_act.jar  ];then
    mv -f ${jarName}_act.jar ${jarName}_act.jar.bak
fi
mv -f ${jarName}.jar ${jarName}_act.jar
cstop ${jarName}
nohup /root/software/jdk/jdk1.8.0_311/bin/java -jar ${jarName}_act.jar --server.port=8070 >> /root/project/$artifactId/nohup 2>&1 &
