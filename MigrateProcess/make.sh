#!/bin/bash

rm src/dslab.jar
cd src
make
jar cf dslab.jar *
