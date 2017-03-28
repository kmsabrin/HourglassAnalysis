package utilityhg;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;

public class ZipfDistributionWrapper {
	public Random random;
	
	public ZipfDistribution zipfDistribution;
	public UniformIntegerDistribution uniformIntegerDistribution;
	
	public NavigableMap<Double, Integer> randomWeightedCollection;
	public double randomWeightedCollectionTotal;
	public boolean alphaNegative;
	public double alpha;
	
	public ZipfDistributionWrapper(int nElements, double a) {
		random = new Random(System.nanoTime());
		
		if (a < 0) {
			alphaNegative = true;
		} else {
			alphaNegative = false;
		}
		this.alpha = Math.abs(a);
		
		zipfDistribution = new ZipfDistribution(nElements, alpha);
		initiateRandomWeightedCollection(nElements, zipfDistribution);
	}

	
//	public int getNodeFromUniformDistribution(int startNodeIndex, int endNodeIndex) {
//		double epsilon = 0.000001;
//		double p = random.nextDouble();
//		double cumulativeProbability = 0;
//		for (int i = startNodeIndex; i <= endNodeIndex; ++i) {
//			cumulativeProbability += uniformIntegerDistribution.probability(i);
//			if (p < cumulativeProbability + epsilon) {
//				return i;
//			}
//		}
//		return endNodeIndex;
//	}
	
	public int getNodeFromZipfDistribution2(int startNodeIndex) {
		double value = random.nextDouble() * randomWeightedCollectionTotal;
        int elementIndex = randomWeightedCollection.ceilingEntry(value).getValue();
//        System.out.println(value + "\t" + elementIndex);
        return startNodeIndex + elementIndex - 1;
	}
	
	public double getProbabilityFromZipfDistribution(int nElements, int nodeIndex) {
		if (alphaNegative == true) {
			nodeIndex = nElements - nodeIndex + 1;
		}
		
		return zipfDistribution.probability(nodeIndex);
	}
	
	private void initiateRandomWeightedCollection(int nElements, ZipfDistribution zipfDistribution) {
		randomWeightedCollection = new TreeMap();
		randomWeightedCollectionTotal = 0;
		for (int i = 1; i <= nElements; ++i) {
			double weight = -1; 
			if (alphaNegative == true) {
				weight = zipfDistribution.probability(nElements - i + 1);
			}
			else {
				weight = zipfDistribution.probability(i);
			}
		    randomWeightedCollectionTotal += weight;
		    randomWeightedCollection.put(randomWeightedCollectionTotal, i);
//		    System.out.println(i + "\t" + randomWeightedCollectionTotal);
		}
	}
}
