#!/bin/bash
# run with one argument (the directory which contains the results for all experiments)

# change to target directory which contains the experiment directories
cd ${1}

# iterate over each experiment folder
for dir in `ls -1d *Experiment`; do

	# change to the experiment directory
	cd ${dir}
	
	# set experiment report file name
	EXPERIMENT_REPORT_FILE="../${dir}.csv"

        # delete experiment report file if it exists
	if [ -f $EXPERIMENT_REPORT_FILE ]; then
		rm $EXPERIMENT_REPORT_FILE
	fi

	# get experiment name
	EXPERIMENT_NAME=`echo ${dir} | cut -d '-' -f 2`
	EXPERIMENT_NAME=`echo $EXPERIMENT_NAME | sed 's/Experiment//' -`

	if [[ "$EXPERIMENT_NAME" =~ ^(MicroNonTopological|MicroSelections|MicroJoins|MicroAggregations|Synthetic|SyntheticOnlyPoints|Scalability|PreGeneratedSynthetic)$ ]]; then
		# list the long.. files only and for each one of them find:
		#     the query number, name and type
		#     the corresponding short.. file from which retrieve the median value
		# and find the correct result line in the long.. file based on the median value
		for file in `ls -1 *long`; do 		
			QUERY_RELNO=`echo $file | cut -d '-' -f 1`
			QUERY_NO=`echo $file | cut -d '-' -f 2 | cut -d '_' -f 1`
			QUERY_NAME=`echo $file | cut -d '-' -f 2 | cut -d '_' -f 2-`
			QUERY_TYPE=`echo $file | cut -d '-' -f 3`
			SHORT_FILE=`echo $file | cut -d '-' -f 1,2,3`
			MEDIAN_VALUE=`cut $SHORT_FILE -d ' ' -f 2`
                        MEDIAN_VALUE_SECS=`bc -l <<< "scale=3; $MEDIAN_VALUE / 10^9"`
			# <===
                        QUERY_RESULTS=`grep -m 1 -e $MEDIAN_VALUE $file | cut -d ' ' -f 1,2,3,4`" $MEDIAN_VALUE_SECS"
			# ==> QUERY_RESULTS=`cut -d ' ' -f 1,2,3,4 $file`
			# display contents
			echo -e "$QUERY_RELNO $QUERY_NO $QUERY_NAME $QUERY_TYPE $QUERY_RESULTS" >> $EXPERIMENT_REPORT_FILE                       
		done

	elif [[ "$EXPERIMENT_NAME" =~ ^(MacroMapSearch)$ ]]; then
		files=( "Get_Around_POIs" "Get_Around_Roads" "Thematic_Search" )
		for file in "${files[@]}"; do 		
			EXPERIMENT_REPORT_FILE="../${dir}_${file}.csv"
			awk '{ print $4 " " $1 " " $2 " " $3}' $file >> $EXPERIMENT_REPORT_FILE
		done
	elif [[ "$EXPERIMENT_NAME" =~ ^(MacroRapidMapping)$ ]]; then
		files=( "Get_CLC_areas" "Get_coniferous_forests_in_fire" "Get_highways" "Get_hotspots" "Get_municipalities" "Get_road_segments_affected_by_fire")
		for file in "${files[@]}"; do 		
			EXPERIMENT_REPORT_FILE="../${dir}_${file}.csv"
			awk '{ print $4 " " $1 " " $2 " " $3}' $file >> $EXPERIMENT_REPORT_FILE
		done
	elif [[ "$EXPERIMENT_NAME" =~ ^(MacroReverseGeocoding)$ ]]; then
		files=( "Find_Closest_Motorway" "Find_Closest_Populated_Place" )
		for file in "${files[@]}"; do 		
			EXPERIMENT_REPORT_FILE="../${dir}_${file}.csv"
			awk '{ print $4 " " $1 " " $2 " " $3}' $file >> $EXPERIMENT_REPORT_FILE
		done	
	else
		echo "Unknown Experiment"
	fi

# change to parent directory
cd ..

done