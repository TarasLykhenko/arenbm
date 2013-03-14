#!/bin/sh

#README
#call: sh script.sh time_limit experiment-a/oracle.log experiment-a/oracle.log experiment-a/oracle.log
#   arguments: 
#       experiment-a/oracle.log experiment-a/oracle.log experiment-a/oracle.log: oracle log files

#note that its converts the output to TB
OUTPUT="/tmp/avg_bwd.txt"
TEMP_OUTPUT="/tmp/temp_output.txt"
TEMP_OUTPUT_2="/tmp/temp_output_2.txt"

rm -rf ${OUTPUT} 
rm -rf ${TEMP_OUTPUT} 
rm -rf ${TEMP_OUTPUT_2} 

#increment a number
inc_number() {
        echo `expr $1 + 1`
}

#note that its converts the output to TB in func convertStorage(bits) { return (bits)/(1024^4) } awk function
process() {
  awk -F',' -v ts=0 -v now=0 -v last_storage=0 -v storage=0 -v trans=0 -v print_date=$2 -v label=$3 -v t_counter=0 'BEGIN {printf("ts,%s\n",label);getline;} { \
    now=$1; \
    if(print_date>0) \
      printf("%i,%.2f\n",t_counter,$2); \
    else \
      printf("%.2f\n",$2); \
    t_counter++; \
  } END {} ' $1

}

LABEL="e"
counter=1

for input in $1 $2 $3 $4
do
  CUR_LABEL=${LABEL}${counter}
  if [ ${counter} = 1 ]; then  
    process ${input} 1 $CUR_LABEL > $OUTPUT
  else
    process ${input} 0 $CUR_LABEL |cut -d, -f2 > $TEMP_OUTPUT
    paste -d, $OUTPUT $TEMP_OUTPUT > $TEMP_OUTPUT_2
    cat $TEMP_OUTPUT_2 > $OUTPUT
  fi
  counter=`inc_number $counter`
done

echo 'done'
