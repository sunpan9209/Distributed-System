#!/bin/bash
echo "Generating point data..."
python RandomPoint.py 1000000 1000 > random.txt
echo "Performing K-means clustering"
java SequentialKMeans points 4 1 random.txt

