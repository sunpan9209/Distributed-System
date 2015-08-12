#!/bin/bash
echo "Generating point data..."
python RandomPoint.py 100000 1000 > random.txt
echo "Performing K-means clustering"
mpirun -np 12 --host ghc41,ghc43 java ParallelKMeans points 4 1 random.txt

