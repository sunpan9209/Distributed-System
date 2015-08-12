#!/bin/bash
echo "Generating DNA data..."
python RandomDNA.py 100000 100 > random.txt
echo "Performing K-means clustering"
mpirun -np 12 --host ghc41,ghc43 java ParallelKMeans dna 4 1 random.txt

