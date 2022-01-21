#!/bin/bash

#echo "##################################################info########################################"
kw=$1
file="/cfind/file"
kwbegin="${kw}-begin"
kwend="${kw}-end"
log="/cfind/log"
date=`date`
echo "$kw" >> $log
if [ -z "$kw" ];then
	echo "${date}  empty kw!" >> $log
	exit 1
fi

tmp="/cfind/tmp"
beginset=`grep -n "$kwbegin" $file |tee ${tmp}`
endset=`grep -n "$kwend" $file`
#echo "beginset: ${beginset}"
#echo "endset: ${endset}"
if [[ -z "$beginset" || -z "$endset" ]];then
	echo "${date}  empty beginset or endset" >> $log
	exit 1
fi
#echo "${beginset}" >$tmp
pasted=`echo "${endset}" | paste -d ":"  ${tmp} - `
#echo "pasted: ${pasted}"

res=""
#enter=$"\n"
for line in ${pasted};do
	beginidx=`echo $line | cut -d ':' -f1`
	endidx=`echo $line | cut -d ':' -f3`
	if [[ -z "$beginidx" || -z "$endidx" ]];then
		#echo "empty"
		continue
	fi
	if [ $beginidx -gt $endidx ];then
		echo "bad index" >> $log
		continue
	fi
	part=`sed -n "${beginidx},${endidx}p" $file`
	#echo "part: $part"
	res=`echo -e "${res}\n//delimit//\n${part}"`
	#res="${res}${part}"
done
#echo "$res" >> $log
echo "$res"