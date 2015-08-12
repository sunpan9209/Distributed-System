#!/bin/bash
echo "Generating DNA data..."
python RandomDNA.py 1000000 100 > random.txt
echo "Performing K-means clustering"
java SequentialKMeans dna 4 1 random.txt

