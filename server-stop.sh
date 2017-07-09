#!/bin/bash
pid=`ps aux | grep BppServer | awk '{print $2}'`
kill -9 $pid
