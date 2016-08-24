package corehg;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;

public class SimpleModelDAG {
	public static Random random = new Random(System.nanoTime());
	public static double alpha = 0.0;
	public static boolean alphaNegative = false;
	public static boolean isMultigraph = false;
	public static boolean isPoisson = true;
	
//	real network matching
	public static int nT = 3; // no. of (T)arget nodes
	public static int nI = 6; // no. of (I)ntermediate nodes
	public static int nS = 3; // no. of (S)ource nodes

	public static int sT = 0; // start of Target
	public static int sI = nT; // start of Intermediate
	public static int sS = nT + nI; // start of Source
	
	public static HashMap<String, Integer> edgeWeights;
	
	public static double ratio = -1;
	
//	toy test
//	static int nT = 3; // no. of T(arget) nodes
//	static int nI = 5; // no. of I(ntermediate) nodes
//	static int nS = 2; // no. of S(ource) nodes
//
//	static int sT = 1; // start of Target
//	static int sI = 4; // start of Intermediate
//	static int sS = 9; // start of source
	
//	simplified analysis model	
//	static int nT = 1; // no. of T(arget) nodes
//	static int nI = 998; // no. of I(ntermediate) nodes
//	static int nS = 1; // no. of S(ource) nodes
//
//	static int sT = 1; // start of Target
//	static int sI = 2; // start of Intermediate
//	static int sS = 1000; // start of source

//	static HashMap<Integer, Integer> outDegree = new HashMap();

	public static PoissonDistribution poissonDistribution;
	public static ZipfDistribution zipfDistribution;
//	static ZipfDistribution zipfDistribution2 = new ZipfDistribution(10, 1.0);
	public static UniformIntegerDistribution uniformIntegerDistribution;
	
//	static double normalMean = 10.0;
//	static double normalSD = 4.0;
//	static NormalDistribution normalDistribution = new NormalDistribution(normalMean, normalSD);

	public static DependencyDAG dependencyDAG;
	
	public static TreeMap<Integer, Integer> inDegreeHistogram;
	public static int numOfNonzeroIndegreeNodes;
	
	public static int din = 2;
	
	public static NavigableMap<Double, Integer> randomWeightedCollection;
	public static double randomWeightedCollectionTotal = 0;
	
	/*
	RandomWeightedCollection randomWeightedCollection;
	
	private static class RandomWeightedCollection {
	    private final NavigableMap<Double, Integer> map = new TreeMap();
	    private final Random random;
	    private double total = 0;

	    public RandomWeightedCollection() {
	        this(new Random());
	    }

	    public RandomWeightedCollection(Random random) {
	        this.random = random;
	    }

	    public void add(double weight, int key) {
	        if (weight <= 0) return;
	        total += weight;
	        map.put(total, key);
	    }

	    public int next() {
	        double value = random.nextDouble() * total;
	        return map.ceilingEntry(value).getValue();
	    }
	}
	*/
	
	public static void getSimpleModelDAG() throws Exception {
		String negate = "";
		if (alphaNegative) negate += "-";
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		
		PrintWriter pw = new PrintWriter(new File("synthetic_callgraphs//SimpleModelDAG" + "r" + df.format(ratio) + "a" + negate + df.format(alpha) + "d" + din + ".txt"));
		edgeWeights = new HashMap();
		generateSimpleModelDAG(pw);
	}
	
	public static int getInDegree() {
//		return din;
		if (isPoisson) {
			int inD = poissonDistribution.sample() + 1;
			return inD;
		}
		else {
			return din;
		}
		
		/*
		int values[] = {2, 3, 4, 5};
//		int values[] = {7, 8, 9, 10};
		return values[random.nextInt(4)];
		
		return (int)Math.ceil(normalDistribution.sample());
		return zipfDistribution2.sample();
		
//		For generating synthetic resembling real networks with given in-degree distribution
		double rv = random.nextDouble();
		double cp = 0;
		for (int i: inDegreeHistogram.keySet()) {
			cp += 1.0 * inDegreeHistogram.get(i) / numOfNonzeroIndegreeNodes;
			if (rv < cp) return i;
		}
		return inDegreeHistogram.get(inDegreeHistogram.firstKey());
		*/
	}
	
	public static int getNodeFromZipfDistribution(int startNodeIndex, int endNodeIndex) {
		int nElements = endNodeIndex - startNodeIndex + 1;
		double epsilon = 0.0; //0.000001;
		double p = random.nextDouble();
		double cumulativeProbability = 0;
		for (int i = 1; i <= nElements; ++i) {
			if (alphaNegative == true) {
				cumulativeProbability += zipfDistribution.probability(nElements - i + 1);
			}
			else {
				cumulativeProbability += zipfDistribution.probability(i);
			}
			
			if (p < cumulativeProbability + epsilon) {
				return startNodeIndex + i - 1;
			}
		}
		
		return endNodeIndex;
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
	
	public static int getNodeFromZipfDistribution2(int startNodeIndex, int endNodeIndex) {
		double value = random.nextDouble() * randomWeightedCollectionTotal;
        int elementIndex = randomWeightedCollection.ceilingEntry(value).getValue();
        return startNodeIndex + elementIndex - 1;
	}
	
	public static void initiateRandomWeightedCollection(int nElements, ZipfDistribution zipfDistribution) {
		randomWeightedCollection = new TreeMap();
//		randomWeightedCollection.clear();
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
//		randomWeightedCollection = new TreeMap();
		double timeSum = 0;
		for (int productIndex = sS - 1; productIndex >= 0; --productIndex) {	
//			System.out.println(productIndex);
			
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
//					System.out.println("Here: " + (endNodeIndex - startNodeIndex + 1) + "\t" + alpha);
					zipfDistribution = new ZipfDistribution(endNodeIndex - startNodeIndex + 1, alpha);
//					double startTime = System.nanoTime();
					initiateRandomWeightedCollection(endNodeIndex - startNodeIndex + 1, zipfDistribution);
//					double endTime = System.nanoTime();
//					timeSum += (endTime - startTime);
//					System.out.println("Elapsed A: " + ((endTime - startTime) / 1000000000.0) );					

				}
			}
			
			double startTime = System.nanoTime();
			int k = getInDegree();
//			System.out.println(k);
			k = Math.min(k, endNodeIndex - startNodeIndex + 1);
			
			for (int j = 0; j < k; ++j) {
				int substrateIndex;
				if (Math.abs(alpha) < 0.000001) {
					substrateIndex = getNodeFromUniformDistribution(startNodeIndex, endNodeIndex);
				} 
				else {
//					substrateIndex = getNodeFromZipfDistribution(startNodeIndex, endNodeIndex);
					substrateIndex = getNodeFromZipfDistribution2(startNodeIndex, endNodeIndex);
//					special case: no order among sources
					if (substrateIndex >= sS) {
						substrateIndex = sS + random.nextInt(nS);
					}
				}
				
				String str = substrateIndex + " " + productIndex;
				
//				if (isMultigraph == false && edgeWeights.containsKey(str)) {
//					--j;
//					System.out.println("Collision!");
//					continue;
//				}
				
				while (isMultigraph == false && edgeWeights.containsKey(str)) {
					++substrateIndex;
					if (substrateIndex > endNodeIndex) {
						substrateIndex = sS + random.nextInt(nS);
//						System.out.println("Oh wow!");
					}
					
					str = substrateIndex + " " + productIndex;
//					System.out.println("Collision!");
				}
				
//				System.out.println(substrateIndex + " " + productIndex);
				
				if (edgeWeights.containsKey(str)) {
					int v = edgeWeights.get(str);
					edgeWeights.put(str, v + 1);
				}
				else {
					edgeWeights.put(str, 1);
				}
			}

//			double endTime = System.nanoTime();
//			System.out.println("Elapsed B: " + ((endTime - startTime) / 1000000000.0) );

		}
		
		for (String key: edgeWeights.keySet()) {
			int w = edgeWeights.get(key);
			pw.println(key + " " + w);
		}
		
		pw.close();
		
//		System.out.println("Time Sum: " + (timeSum / 1000000000.0) );
	}
	
	public static void initNodeIdentifiers(int nT, int nI, int nS, int din) {
		SimpleModelDAG.nT = nT;
		SimpleModelDAG.nI = nI;
		SimpleModelDAG.nS = nS;
		SimpleModelDAG.sT = 0; // start of Target
		SimpleModelDAG.sI = nT; // start of Intermediate
		SimpleModelDAG.sS = nT + nI; // start of Source
		SimpleModelDAG.din = din;
	}
	
	public static void generateSimpleModel(double alpha, int din, int nT, int nI, int nS, double ratio) throws Exception {
		SimpleModelDAG.alpha = alpha;
		SimpleModelDAG.din = din;
		SimpleModelDAG.ratio = ratio;
		initNodeIdentifiers(nT, nI, nS, din);
		
		poissonDistribution = new PoissonDistribution(din);
		
		if (alpha < 0) {
			alphaNegative = true;
		} else {
			alphaNegative = false;
		}
		SimpleModelDAG.alpha = Math.abs(alpha);

		random = new Random(System.nanoTime());

		getSimpleModelDAG();
	}

	public static void main(String[] args) throws Exception {
//		double alphaValues[] = {-1.0, -0.5, 0, 0.5, 1.0};
		double alphaValues[] = {0.0};
		
		poissonDistribution = new PoissonDistribution(din);
		
		for (double d : alphaValues) {
			if (d < 0) {
				alphaNegative = true;
			} else {
				alphaNegative = false;
			}
			alpha = Math.abs(d);
			random = new Random(System.nanoTime());

			getSimpleModelDAG();

			// break;
		}
		System.out.println("Done!");
	}
}
