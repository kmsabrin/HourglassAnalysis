package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.Random;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;

public class SyntheticNLDAG2 {
	static Random random = new Random(System.nanoTime());
	static double alpha = 0.5;
	
//	static int nT = 200; // no. of T(arget) nodes
//	static int nI = 1600; // no. of I(ntermediate) nodes
//	static int nS = 200; // no. of S(ource) nodes
//
//	static int sT = 1; // start of Target
//	static int sI = 201; // start of Intermediate
//	static int sS = 1801; // start of source
	
//	toy
	static int nT = 4; // no. of T(arget) nodes
	static int nI = 8; // no. of I(ntermediate) nodes
	static int nS = 4; // no. of S(ource) nodes

	static int sT = 1; // start of Target
	static int sI = 5; // start of Intermediate
	static int sS = 13; // start of source

	
	public static void getNLNHGDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("synthetic_callgraphs//NLNHGDAGa" + alpha + ".txt"));
		generateNLDAG(pw);
	}
	
	public static int getInDegree() {
		int values[] = {1, 2, 3, 4, 5};
//		int values[] = {7, 8, 9, 10};
		return values[random.nextInt(2)];
	}
	
	public static int getNodeFromZipfDistribution(int startNodeIndex, int endNodeIndex) {
		int nElements = endNodeIndex - startNodeIndex + 1;
		double epsilon = 0.000001;
		ZipfDistribution zipfDistribution = new ZipfDistribution(nElements, alpha);
		double p = random.nextDouble();
		
		double cumulativeProbability = 0;
		for (int i = 1; i <= nElements; ++i) {
			cumulativeProbability += zipfDistribution.probability(i);
			if (p < cumulativeProbability + epsilon) {
//				System.out.println(p + "\t" + i + "\t" + zipfDistribution.probability(i) + "\t" + cumulativeProbability);
				return startNodeIndex + i - 1;
			}
		}
		
		return endNodeIndex;
	}
	
	public static int getNodeFromUniformDistribution(int startNodeIndex, int endNodeIndex) {
		double epsilon = 0.000001;
		UniformIntegerDistribution uniformIntegerDistribution = new UniformIntegerDistribution(startNodeIndex, endNodeIndex);
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
			int k = getInDegree();
			
			for (int j = 0; j < k; ++j) {
				int substrateIndex;
				if (productIndex < sI) {
					if (alpha > 0) {
						substrateIndex = getNodeFromZipfDistribution(sI, sS + nS - 1);
					}
					else {
						substrateIndex = getNodeFromUniformDistribution(sI, sS + nS - 1);
					}
				}
				else {
					if (alpha > 0) {
						substrateIndex = getNodeFromZipfDistribution(productIndex + 1, sS + nS - 1);
					}
					else {
						substrateIndex = getNodeFromUniformDistribution(productIndex + 1, sS + nS - 1);
					}
				}
				pw.println(substrateIndex + " " + productIndex);
			}
		}
		
		pw.close();
	}

	public static void main(String[] args) throws Exception {
		getNLNHGDAG();
		System.out.println("Done!");
	}
}
