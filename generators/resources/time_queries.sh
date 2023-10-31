#!/bin/bash

for i in `seq 0 35` ;
do
	if [[ ! -a "${i}.err" ]] ; then
		continue;
	fi

	start=`grep FunctionRegistry ${i}.err |tail -n 1|cut -d \  -f 1,2`
	stop=`grep PostGISEvaluation ${i}.err |tail -n 1|cut -d \  -f 1,2`

	S=`date --date "${start}" +%s`
	T=`date --date "${stop}" +%s`

	D="$((T-S))"

	r=`wc -l ${i}.out|cut -d \  -f 1`
	results=$((r-1))

	echo "Q${i} ${D} seconds (${results} results)"
done
