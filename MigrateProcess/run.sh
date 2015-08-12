#!/bin/bash

if [ ! -z $1 ] 
then 
    java -cp src/dslab.jar edu/cmu/cs/cs440/p1/main/Main -s $1
else
    java -cp src/dslab.jar edu/cmu/cs/cs440/p1/main/Main
fi
