#!/bin/bash

# declare an associative array that returns the last portion of the 
# data source directory when indexed with the database name
declare -A listDirsArr
listDirsArr=( 
#               [realworld]="RealWorldWorkload/NO_CRS/RealWorld" 
#              [realworld_points]="RealWorldWorkload/NO_CRS/RealWorld_Points" 
#              [synthetic]="SyntheticWorkload/Synthetic" 
#              [synthetic_pois]="SyntheticWorkload/Synthetic_POIs"
#              [census]="Census/NO_CRS"
               [scalability_10K]="Scalability/10K"
#              [scalability_100K]="Scalability/100K"
#              [scalability_1M]="Scalability/1M"
#              [scalability_10M]="Scalability/10M"
#              [scalability_100M]="Scalability/100M"
#              [scalability_500M]="Scalability/500M"
)

# Looping through keys and values in the associative array <listDirsArr>
for db in "${!listDirsArr[@]}"; do
    echo "Creating database \"${db}\" by importing files from \"${DatasetBaseDir}/${listDirsArr[${db}]}/\"";
    ./createVirtuosoRepo.sh -d ${db} ${DatasetBaseDir}/${listDirsArr[${db}]} |& tee -a createRepo_${db}.log
done
