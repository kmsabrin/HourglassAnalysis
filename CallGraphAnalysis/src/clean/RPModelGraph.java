package clean;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;

public class RPModelGraph {
	public static Random random = new Random(System.nanoTime());
	public static double alpha = 0.0;
	public static boolean alphaNegative = false;
	public static boolean isPoisson = true;

	public static int nT; // no. of (T)arget nodes
	public static int nI; // no. of (I)ntermediate nodes
	public static int nS; // no. of (S)ource nodes
	public static int sT; // start of Target
	public static int sI; // start of Intermediate
	public static int sS; // start of Source
	
	public static HashSet<String> uniqueEdge;
		
	public static PoissonDistribution poissonDistribution;
	public static ZipfDistribution zipfDistribution;
	public static UniformIntegerDistribution uniformIntegerDistribution;
	
	public static int din = 2;	
	public static NavigableMap<Double, Integer> randomWeightedCollection;
	public static double randomWeightedCollectionTotal = 0;

	public static int getInDegree() {
//		return din + 1;
		if (isPoisson) {
			int inD = poissonDistribution.sample() + 1;
			return inD;
		}
		else {
			return din;
		}
	}
	
	public static int getNodeFromUniformDistribution(int startNodeIndex, int endNodeIndex) {
		double epsilon = 0.000001;
		double p = random.nextDouble();
		double cumulativeProbability = 0;
		for (int i = startNodeIndex; i <= endNodeIndex; ++i) {
			cumulativeProbability += uniformIntegerDistribution.probability(i);
			if (p < cumulativeProbability + epsilon) {
				return i;
			}
		}
		
		return endNodeIndex;
	}
	
	private static int getNodeFromZipfDistribution2(int startNodeIndex) {
		double value = random.nextDouble() * randomWeightedCollectionTotal;
        int elementIndex = randomWeightedCollection.ceilingEntry(value).getValue();
        return startNodeIndex + elementIndex - 1;
	}
	
	public static void initiateRandomWeightedCollection(int nElements, ZipfDistribution zipfDistribution) {
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
		}
	}
	
	public static void generateSimpleModelDAG(PrintWriter pw) throws Exception {	
		for (int productIndex = sS - 1; productIndex >= 0; --productIndex) {	
			int startNodeIndex = -1;
			if (productIndex < sI) { // choosing substrates for targets
				startNodeIndex = sI;
			}
			else { // choosing substrates for intermediate nodes
				startNodeIndex = productIndex + 1;
			}
			int endNodeIndex = sS + nS - 1;
			
			if (productIndex >= sI - 1) { // don't regenerate for targets
				if (Math.abs(alpha) < 0.000001) { // uniform distribution
					if (startNodeIndex < endNodeIndex) {
						uniformIntegerDistribution = new UniformIntegerDistribution(startNodeIndex, endNodeIndex);
					}
				} 
				else { // zipf distribution
					zipfDistribution = new ZipfDistribution(endNodeIndex - startNodeIndex + 1, alpha);
					initiateRandomWeightedCollection(endNodeIndex - startNodeIndex + 1, zipfDistribution);					
				}
			}
			
			int k = getInDegree();
//			if (productIndex > nS - 10) System.out.println(k);
			k = Math.min(k, endNodeIndex - startNodeIndex + 1);
			
			
			for (int j = 0; j < k; ++j) {
				int substrateIndex;
				if (Math.abs(alpha) < 0.000001) {
					substrateIndex = getNodeFromUniformDistribution(startNodeIndex, endNodeIndex);
				} 
				else {
					substrateIndex = getNodeFromZipfDistribution2(startNodeIndex);
					
//					ensure no order among sources
					if (substrateIndex >= sS) {
						substrateIndex = sS + random.nextInt(nS);
					}
				}
				
				String str = substrateIndex + " " + productIndex;		
				
				while (uniqueEdge.contains(str)) {
					if (substrateIndex >= sS) {
						substrateIndex = sS + random.nextInt(nS);
					}
					else {
						++substrateIndex;
						if (substrateIndex > endNodeIndex) {
							substrateIndex = sS + random.nextInt(nS);
						}
					}
					str = substrateIndex + " " + productIndex;
				}				
				
				uniqueEdge.add(str);
			}
		}
		
		for (String key: uniqueEdge) {
			pw.println(key);
		}
		pw.close();
	}
	
	private static void printSourceTarget() throws Exception {
		PrintWriter pw = new PrintWriter(new File("data//RPModelDAG_targets.txt"));
		for (int i = 0; i < sI; ++i) {
			pw.println(i);
		}
		pw.close();
		
		pw = new PrintWriter(new File("data//RPModelDAG_sources.txt"));
		for (int i = sS; i < nT+nI+nS; ++i) {
			pw.println(i);
		}
		pw.close();
	}
	
	public static void generateSimpleModel(double alpha, int din, int nT, int nI, int nS) throws Exception {
		RPModelGraph.nT = nT;
		RPModelGraph.nI = nI;
		RPModelGraph.nS = nS;
		RPModelGraph.sT = 0; // start of Target
		RPModelGraph.sI = nT; // start of Intermediate
		RPModelGraph.sS = nT + nI; // start of Source
		RPModelGraph.din = din;
		RPModelGraph.alpha = alpha;
		
		poissonDistribution = new PoissonDistribution(din);

		String negate = "";
		if (alpha < 0) {
			alphaNegative = true;
			negate += "-";
		} else {
			alphaNegative = false;
		}
		RPModelGraph.alpha = Math.abs(alpha);

		random = new Random(System.nanoTime());
		
		PrintWriter pw = new PrintWriter(new File("data//RPModelDAG_links.txt"));
		uniqueEdge = new HashSet();
		generateSimpleModelDAG(pw);
		printSourceTarget();
	}

	public static void main(String[] args) throws Exception {
		for (double i = 1.8; i < 2.1; i += 0.2) {
			generateSimpleModel(i, 1, 250, 250, 500);
			HourglassAnalysis hourglassAnalysis = new HourglassAnalysis();
			hourglassAnalysis.runAnalysis("RPModelDAG");
		}
	}
}
