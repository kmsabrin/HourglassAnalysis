package utilityhg;

import java.io.File;
import java.io.PrintWriter;
import java.util.Random;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class AlmostZipfModel {
	static int nSources;
	static int nTargets;
	static int nIntermediates;
	static int nNodes;
	static int alpha;
	static double bias = 0.5;
	static PoissonDistribution poissonDistribution;
	
	private static void getSyntheticNetwork() throws Exception {
		Random rand = new Random(System.nanoTime());
		int end = nTargets + nIntermediates;
		PrintWriter pw = new PrintWriter(new File("synthetic_callgraphs//SimpleModelDAG" + "n" + nNodes + "a" + alpha + ".txt"));	
		
		for (int i = (nTargets + nIntermediates - 1); i >= 0; --i) {
			int start = i + 1;
			int inDeg = poissonDistribution.sample() + 1;
			if (i < nTargets) {
				start = nTargets;
			}
			for (int j = 0; j < inDeg; ++j) {
				if (alpha > 0) {
					for (int k = start; k <= end; ++k) {
						if (rand.nextDouble() < bias) {
							int substrate = k;
							if (substrate == end) {
								substrate = end + rand.nextInt(nSources);
							}
							pw.println(substrate + "\t" + i);
							break;
						}
					}
				}
				else if (alpha < 0){
					for (int k = end; k >= start; --k) {
						if (rand.nextDouble() < bias) {
							int substrate = k;
							if (substrate == end) {
								substrate = end + rand.nextInt(nSources);
							}
							pw.println(substrate + "\t" + i);
							break;
						}
					}
				}
				else {
					int substrate = start + rand.nextInt(end - start + 1);
					if (substrate == end) {
						substrate = end + rand.nextInt(nSources);
					}
					pw.println(substrate + "\t" + i);
				}
			}
		}

		pw.close();
	}
	
	public static void generateNetwork(int a, int din, int nT, int nI, int nS, int nN) throws Exception {
		nSources = nS;
		nTargets = nT;
		nIntermediates = nI;
		nNodes = nN;
		alpha = a;
		poissonDistribution = new PoissonDistribution(din);
		getSyntheticNetwork();
	}
	
	public static void main(String[] args) throws Exception {
		generateNetwork(0, 1, 4, 4, 4, 12);
	}
}
