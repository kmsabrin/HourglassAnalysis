package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;

public class SyntheticNLDAG2 {
	static Random random = new Random(System.nanoTime());
	static double alpha = 1.0;
	
//	static int nT = 200; // no. of T(arget) nodes
//	static int nI = 1600; // no. of I(ntermediate) nodes
//	static int nS = 200; // no. of S(ource) nodes
//
//	static int sT = 1; // start of Target
//	static int sI = 201; // start of Intermediate
//	static int sS = 1801; // start of source
	
//	toy
//	static int nT = 4; // no. of T(arget) nodes
//	static int nI = 8; // no. of I(ntermediate) nodes
//	static int nS = 4; // no. of S(ource) nodes
//
//	static int sT = 1; // start of Target
//	static int sI = 5; // start of Intermediate
//	static int sS = 13; // start of source
	
	static int nT = 1; // no. of T(arget) nodes
	static int nI = 998; // no. of I(ntermediate) nodes
	static int nS = 1; // no. of S(ource) nodes

	static int sT = 1; // start of Target
	static int sI = 2; // start of Intermediate
	static int sS = 1000; // start of source

	static HashMap<Integer, Integer> outDegree = new HashMap();
	
	static ZipfDistribution zipfDistribution;
	static UniformIntegerDistribution uniformIntegerDistribution;
	
	public static void getNLNHGDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("synthetic_callgraphs//NLNHGDAGa" + alpha + ".txt"));
		generateNLDAG(pw);
	}
	
	public static int getInDegree() {
//		int values[] = {1, 2, 3, 4, 5};
//		int values[] = {7, 8, 9, 10};
//		return values[random.nextInt(2)];
		return 3;
	}
	
	public static int getNodeFromZipfDistribution(int startNodeIndex, int endNodeIndex) {
		int nElements = endNodeIndex - startNodeIndex + 1;
		double epsilon = 0.0; //0.000001;
		double p = random.nextDouble();
		double cumulativeProbability = 0;
		for (int i = 1; i <= nElements; ++i) {
			cumulativeProbability += zipfDistribution.probability(i);
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
	
	public static void generateNLDAG(PrintWriter pw) throws Exception {				
		for (int productIndex = sS - 1; productIndex > 0; --productIndex) {			
			int startNodeIndex = -1, endNodeIndex = -1;
			if (productIndex < sI) {
				startNodeIndex = sI;
			}
			else {
				startNodeIndex = productIndex + 1;
			}
			endNodeIndex = sS + nS - 1;
			zipfDistribution = new ZipfDistribution(endNodeIndex - startNodeIndex + 1, alpha);
			
			if (startNodeIndex < endNodeIndex) {
				uniformIntegerDistribution = new UniformIntegerDistribution(startNodeIndex, endNodeIndex);
			}
			else {
				outDegree.put(sS, 1);
				continue;
			}
			
			int k = getInDegree();
			
			for (int j = 0; j < k; ++j) {
				int substrateIndex;
				if (alpha > 0) {
					substrateIndex = getNodeFromZipfDistribution(startNodeIndex, endNodeIndex);
				} 
				else {
					substrateIndex = getNodeFromUniformDistribution(startNodeIndex, endNodeIndex);
				}
				pw.println(substrateIndex + " " + productIndex);

				if (outDegree.containsKey(substrateIndex)) {
					outDegree.put(substrateIndex, outDegree.get(substrateIndex) + 1);
				} 
				else {
					outDegree.put(substrateIndex, 1);
				}
			}
		}
		
		pw.close();
	}

	public static void main(String[] args) throws Exception {
		getNLNHGDAG();
		
		int N = nT + nI + nS;
		for (int nodeIndex = sS; nodeIndex > 0; --nodeIndex) {
//			System.out.println(nodeIndex);
			double expectedOutDeg = 0;
			for (int product = nodeIndex - 1; product > 0; --product) {
				ZipfDistribution zipfDistribution = new ZipfDistribution(N - product, alpha);				
				expectedOutDeg += 1.0 - Math.pow(1.0 - zipfDistribution.probability(nodeIndex - product), getInDegree());
			
//				if (product + 1 < N) {
//					UniformIntegerDistribution uniformIntegerDistribution = new UniformIntegerDistribution(product + 1, N);
//					expectedOutDeg += 1.0 - Math.pow(1.0 - uniformIntegerDistribution.probability(nodeIndex), getInDegree());
//				}
//				else {
//					expectedOutDeg = 1;
//				}
				
//				System.out.print(product + "\t");
//				System.out.print(uniformIntegerDistribution.probability(nodeIndex) + "\t");
//				System.out.print(1.0 - uniformIntegerDistribution.probability(nodeIndex) + "\t");
//				System.out.print(Math.pow(1.0 - uniformIntegerDistribution.probability(nodeIndex),getInDegree()) + "\t");
//				System.out.println(1.0 - Math.pow(1.0 - uniformIntegerDistribution.probability(nodeIndex),getInDegree()));
			}
			
			System.out.println(nodeIndex + "\t" + outDegree.get(nodeIndex) + "\t" + expectedOutDeg);

		}
		
		System.out.println("Done!");
	}
}
