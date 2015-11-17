package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;

public class SimpleModelDAG {
	static Random random = new Random(System.nanoTime());
	static double alpha = 0.0;
	static boolean alphaNegative = false;
	
//	real network matching
	static int nT = 2550; // no. of (T)arget nodes
	static int nI = 2440; // no. of (I)ntermediate nodes
	static int nS = 2000; // no. of (S)ource nodes

	static int sT = 0; // start of Target
	static int sI = nT; // start of Intermediate
	static int sS = nT + nI; // start of source
	
	static HashMap<String, Integer> edgeWeights;
	
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
	
	static ZipfDistribution zipfDistribution;
//	static ZipfDistribution zipfDistribution2 = new ZipfDistribution(10, 1.0);
	static UniformIntegerDistribution uniformIntegerDistribution;
	
//	static double normalMean = 10.0;
//	static double normalSD = 4.0;
//	static NormalDistribution normalDistribution = new NormalDistribution(normalMean, normalSD);

	static DependencyDAG dependencyDAG;
	
	static TreeMap<Integer, Integer> inDegreeHistogram;
	static int numOfNonzeroIndegreeNodes;
	
	public static void getSimpleModelDAG() throws Exception {
		String negate = "";
		if (alphaNegative) negate += "-";
		
		PrintWriter pw = new PrintWriter(new File("synthetic_callgraphs//SimpleModelDAGa" + negate + alpha + ".txt"));
		edgeWeights = new HashMap();
		generateSimpleModelDAG(pw);
	}
	
	public static int getInDegree() {
		return 3;
		
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
	
	public static void generateSimpleModelDAG(PrintWriter pw) throws Exception {	
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
			
			if (Math.abs(alpha) < 0.000001) { // uniform distribution
				if (startNodeIndex < endNodeIndex) {
					uniformIntegerDistribution = new UniformIntegerDistribution(startNodeIndex, endNodeIndex);
				}
				/*
				else {
					outDegree.put(sS, 1);
					continue;
				}
				*/
			}
			else { // zipf distribution				
				zipfDistribution = new ZipfDistribution(endNodeIndex - startNodeIndex + 1, alpha);
			}
			
			int k = getInDegree();
//			System.out.println(k);
			
			for (int j = 0; j < k; ++j) {
				int substrateIndex;
				if (Math.abs(alpha) < 0.000001) {
					substrateIndex = getNodeFromUniformDistribution(startNodeIndex, endNodeIndex);
				} 
				else {
					substrateIndex = getNodeFromZipfDistribution(startNodeIndex, endNodeIndex);
					// special case: no order among sources
					if (substrateIndex >= sS) {
						substrateIndex = sS + random.nextInt(nS);
					}
				}
				
				String str = substrateIndex + " " + productIndex;
				if (edgeWeights.containsKey(str)) {
					int v = edgeWeights.get(str);
					edgeWeights.put(str, v + 1);
				}
				else {
					edgeWeights.put(str, 1);
				}
				
				
//				pw.println(productIndex + " -> " + substrateIndex + ";");

				/*
				if (outDegree.containsKey(substrateIndex)) {
					outDegree.put(substrateIndex, outDegree.get(substrateIndex) + 1);
				} 
				else {
					outDegree.put(substrateIndex, 1);
				}
				*/
			}
		}
		
		for (String key: edgeWeights.keySet()) {
			int w = edgeWeights.get(key);
			pw.println(key + " " + w);
		}
		
		pw.close();
	}

	public static void main(String[] args) throws Exception {
//		double alphaValues[] = {-5, -1, 0, 1, 5};
		double alphaValues[] = {0.6};
		
		for (double d: alphaValues) {
			if (d < 0) {
				alphaNegative = true;
			}
			else {
				alphaNegative = false;
			}
			alpha = Math.abs(d);
			random = new Random(System.nanoTime());
			
			getSimpleModelDAG();
			
//			break;
		}
		
		/*
		int N = nT + nI + nS;
		for (int nodeIndex = sS; nodeIndex > 0; --nodeIndex) {
			double expectedOutDeg = 0;
			for (int product = nodeIndex - 1; product > 0; --product) {
				if (alpha > 0) {
					ZipfDistribution zipfDistribution = new ZipfDistribution(N - product, alpha);
					expectedOutDeg += 1.0 - Math.pow(1.0 - zipfDistribution.probability(nodeIndex - product), getInDegree());
				} 
				else {
					if (product + 1 < N) {
						UniformIntegerDistribution uniformIntegerDistribution = new UniformIntegerDistribution(product + 1, N);
						expectedOutDeg += 1.0 - Math.pow(1.0 - uniformIntegerDistribution.probability(nodeIndex), getInDegree());
					} else {
						expectedOutDeg = 1;
					}
				}
			}
			System.out.println(nodeIndex + "\t" + outDegree.get(nodeIndex) + "\t" + expectedOutDeg);
		}
		*/
		
		System.out.println("Done!");
	}
}
