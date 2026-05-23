#!/bin/bash

#replace with your own path to openssl-3.5 and softhsm2.conf   

export OPENSSL_HOME=$HOME/openssl-3.5

export LD_LIBRARY_PATH=$OPENSSL_HOME/lib64:$OPENSSL_HOME/lib

export OPENSSL_MODULES=$OPENSSL_HOME/lib64/ossl-modules

export SOFTHSM2_CONF=/home/mansi/softhsm/softhsm2.conf


echo "Environment Loaded"
