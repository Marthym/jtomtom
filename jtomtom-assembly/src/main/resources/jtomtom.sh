#!/bin/sh
RUN_DIR=$(dirname $(readlink -f "$0"))
exec java -cp $RUN_DIR/.:$RUN_DIR/lib/* org.jtomtom.JTomtom