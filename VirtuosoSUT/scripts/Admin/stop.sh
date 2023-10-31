#! /bin/bash

# Stops the Virtuoso Server instance

# Get Virtuoso Server instance PID
VPID=`pgrep -f virtuoso-t`

# First attempt to TERM and then KILL
kill -TERM ${VPID}
if test $? -ne 0; then
    kill -KILL ${VPID}
    action="killed"
else
    action="terminated"
fi

echo "Virtuoso Server with pid=${VPID} has been ${action}."