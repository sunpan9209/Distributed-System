#!/bin/bash

find src/ -iname *.class -exec rm '{}' ';'
rm src/dslab.jar
mkdir tmp
cp file/p1.txt tmp/p1.txt
rm -r file/*
cp tmp/p1.txt file/p1.txt
rm -r tmp
