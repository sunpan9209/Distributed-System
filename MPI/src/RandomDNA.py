import random
import sys
def main():
    num_strands = int(sys.argv[1])
    strand_length = int(sys.argv[2])
    letters = 'atcg'
    randomDNA = []

    for i in xrange(0, num_strands):
        dna = ''
        for l in xrange(0, strand_length):
            c = letters[random.randint(0, 3)]
            dna+=(c)
        randomDNA.append(dna)
    s = ('\n'.join(randomDNA))
    print(s)
main()
