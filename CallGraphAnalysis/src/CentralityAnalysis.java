import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.math3.stat.StatUtils;

public class CentralityAnalysis {
	double pathCentralityValues[];
	String pathNodes[];
	int pathHopCount;
	HashSet<String> visitedNodes;		
	Random random;
	
	// for average and scatter
	void getLocationVsCentrality(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("Results//loc-vs-centrality-" + filePath + ".txt"));
		
		for (String s: callDAG.nodeCentrality.keySet()) {
			if (callDAG.callTo.containsKey(s) && callDAG.callFrom.containsKey(s)) {
				pw.println(callDAG.location.get(s) + "\t" + callDAG.nodeCentrality.get(s));
			}
			else {
				pw.println(callDAG.location.get(s) + "\t" + 0.0);
			}
		}
		
		pw.close();
	}
	
	void getCentralityCDF(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("Results//centrality-cdf-" + filePath + ".txt"));
		
		for (String s: callDAG.location.keySet()) {
			if (callDAG.callTo.containsKey(s) && callDAG.callFrom.containsKey(s)) {
				pw.println(callDAG.nodeCentrality.get(s));	
			}
			else {
				pw.println(0.0);
			}
		}
		
		pw.close();
	}
	
	void getKernelBoundaryCentralityCDF(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw1 = new PrintWriter(new File("Results//root-centrality-cdf-" + filePath + ".txt"));
		PrintWriter pw2 = new PrintWriter(new File("Results//leaf-centrality-cdf-" + filePath + ".txt"));
				
		for (String s: callDAG.location.keySet()) {
			if (!callDAG.callFrom.containsKey(s)) {
				pw1.println(callDAG.nodeCentrality.get(s));	
			}
			else if (!callDAG.callTo.containsKey(s)) {
				pw2.println(callDAG.nodeCentrality.get(s));
			}
		}
		
		pw1.close();
		pw2.close();
	}
	
	void traverse(String node, CallDAG callDAG) {
		if (!callDAG.callTo.containsKey(node)) {
			pathCentralityValues[pathHopCount] = callDAG.nodeCentrality.get(node);
			pathNodes[pathHopCount] = node;
			++pathHopCount;
			return;
		}
		
		pathCentralityValues[pathHopCount] = callDAG.nodeCentrality.get(node);
		pathNodes[pathHopCount] = node;
		++pathHopCount;
		
//		uniformly sampling of paths
		String functions[] = callDAG.callTo.get(node).toArray(new String[callDAG.callTo.get(node).size()]);
		double rn = random.nextDouble();
		double cumulativeSum = 0;
		for (String s: functions) {			
			cumulativeSum += callDAG.numOfLeafPath.get(s) / callDAG.numOfLeafPath.get(node);
			if (rn < cumulativeSum) {
				traverse(s, callDAG);
				break;
			}
		}
		
//		random (non-uniform) sampling of paths
//		String functions[] = callDAG.callTo.get(node).toArray(new String[callDAG.callTo.get(node).size()]);
//		traverse(functions[random.nextInt(functions.length)], callDAG);
	}
		
	void getSampledPathStatistics(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw1 = new PrintWriter(new File("Results//uniform_path-hscore-cdf-" + filePath + ".txt"));
		PrintWriter pw2 = new PrintWriter(new File("Results//uniform_path-centrality-range-cdf-" + filePath + ".txt"));

//		PrintWriter pw1 = new PrintWriter(new File("Results//random_path-hscore-cdf-" + filePath + ".txt"));
//		PrintWriter pw2 = new PrintWriter(new File("Results//random_path-centrality-range-cdf-" + filePath + ".txt"));
		
		random = new Random(System.nanoTime());
		int samplePathCount = 0;
		int samplePathSize = 1*100000;
		double pathLength[] = new double[samplePathSize];
		double pathHScore[] = new double[samplePathSize];
		double pathCentralityRange[] = new double[samplePathSize];
		double pathMaxCentrality[] = new double[samplePathSize];
		
		while (samplePathCount < samplePathSize) {			
			for (String s : callDAG.location.keySet()) {
				if (callDAG.callFrom.containsKey(s))
					continue; // start from roots only to trace a path
				
				/* remember, the actual centrality values of roots and leaves is ZERO now, although they 
				 * haven't been changed for use like below
				 */
				double rn = random.nextDouble();
				
				// uniformly sampling paths
				if (rn > callDAG.numOfLeafPath.get(s) / callDAG.nTotalPath)
					continue; 

				// randomly sampling paths
//				if (rn > 0.5)
//					continue; 

				pathCentralityValues = new double[10000]; // max possible path length (assumed)
				pathNodes = new String[10000];
				pathHopCount = 0;
				traverse(s, callDAG); // traverse the path

				if (pathHopCount < 3)
					continue; // avoid paths of less than 3 hops
				double pathValues[] = Arrays.copyOfRange(pathCentralityValues, 0, pathHopCount);
				pathValues[0] = pathValues[pathValues.length - 1] = 0; /** make root and leaf zero !!!! **/
				
				/*****************************************/
				/*********** get path H-Score ************/
				/*****************************************/
				double hScore = getHScore(pathValues);
				pw1.println(hScore);
//				for (int i = 0; i < pathHopCount; ++i) System.out.print(pathValues[i] + " -> "); System.out.println();
//				System.out.println(pathHopCount + "\t" + hScore);
				
				/*****************************************/
				/******* get path Centrality Range *******/
				/*****************************************/
				double maxCentrality = StatUtils.max(pathValues);
				pw2.println(maxCentrality);
				
				/*****************************************/
				pathLength[samplePathCount] = pathHopCount;
				pathHScore[samplePathCount] = hScore;
				pathCentralityRange[samplePathCount] = maxCentrality; //centralityRange; CHANGED!
				pathMaxCentrality[samplePathCount] = maxCentrality;
				++samplePathCount;
//				System.out.println(samplePathCount);
				if (samplePathCount >= samplePathSize) break;				
			}
		}
		
//		System.out.println(new PearsonsCorrelation().correlation(pathLength, pathHScore));
//		System.out.println(new PearsonsCorrelation().correlation(pathLength, pathCentralityRange));
//		System.out.println(new PearsonsCorrelation().correlation(pathHScore, pathCentralityRange));
//		System.out.println(new PearsonsCorrelation().correlation(pathHScore, pathMaxCentrality));
		
		pw1.close();
		pw2.close();
	}
		
	static double getHScore(double[] values) {
		int maxIndex = 0;
		double maxCentrality = 0;
		int minDistance = values.length + 1;
		
		for (int i = 0; i < values.length; ++i) {
			if (values[i] > maxCentrality) {
				maxIndex = i;
				maxCentrality = values[i];
			} else if (values[i] < maxCentrality) {
			} else {
				int relativeDistance = Math.abs(values.length / 2 - i);
				if (relativeDistance < minDistance) {
					maxIndex = i;
					maxCentrality = values[i];
					minDistance = relativeDistance;
				}
			}
		}
		
//		System.out.println(maxIndex);
		
		// increasing
		double agree = 0, disagree = 0;
		for (int i = 0; i <= maxIndex; ++i) {
			for (int j = i + 1; j <= maxIndex; ++j) {
				if (values[j] < values[i]) {
					++disagree;
				}
				else ++agree;
			}
		}
		double n = maxIndex + 1;
		double nPairs = (n * (n - 1)) / 2;
		double increasingScore = 1;
		if (n > 1) increasingScore = (agree - disagree) / nPairs;

		// decreasing
		agree = 0; disagree = 0;
		for (int i = maxIndex; i < values.length; ++i) {
			for (int j = i + 1; j < values.length; ++j) {
				if (values[j] > values[i]) ++disagree;
				else ++agree;
			}
		}
		n = values.length - maxIndex;
		nPairs = (n * (n - 1)) / 2;
		double decreasingScore = 1;
		if (n > 1) decreasingScore = (agree - disagree) / nPairs;
		
//		System.out.println(increasingScore + "\t" + decreasingScore);
		return (increasingScore + decreasingScore) / 2.0;
	}
		
	static void testHScore() {
		double values1[] = {1, 2, 3, 1, 3, 1, 3};
//		double values1[] = {0.0,9.757967486678622E-8,6.540269365766033E-8,1.3948602180151594E-6,0.05961269696487076,0.6626932464427995,0.6626947666979051,0.11999062491856848,0.0};
//		double values1[] = {4.6213516589140647E-10,1.4219654823765308E-10,2.2779034562169096E-8,2.2913256043626685E-5,1.718494204546398E-5,0.06930266591684534};
		System.out.println(getHScore(values1));
	}
	
	void subtreeSizeTraverse(String node, CallDAG callDAG) {
		if (visitedNodes.contains(node)) return;
		
		if (!callDAG.callTo.containsKey(node)) {
			visitedNodes.add(node);
			pathHopCount++;
			return;
		}
		
		visitedNodes.add(node);		
		pathHopCount++;
		
		for (String s: callDAG.callTo.get(node)) {
			subtreeSizeTraverse(s, callDAG);
		}		
	}
	
	void getSubtreeSizeCDF(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//subtree-size-cdf-" + filePath + ".txt"));
		for (String s: callDAG.location.keySet()) {
			if (callDAG.callFrom.containsKey(s)) continue;
			pathHopCount = 0;
			visitedNodes = new HashSet();
			subtreeSizeTraverse(s, callDAG);
			pw.println(pathHopCount);
		}
		pw.close();
	}
	
	void test(CallDAG callDAG) {
		double sum = 0;
		System.out.println((int)callDAG.nRoots);
		double values[] = new double[(int)callDAG.nRoots*2];
		int indx = 0;
		for (String s: callDAG.location.keySet()) {
			if (callDAG.callFrom.containsKey(s)) continue;
			sum += callDAG.nodeCentrality.get(s);
			values[indx++] = callDAG.nodeCentrality.get(s);
		}
		System.out.println(indx);
		System.out.println(StatUtils.min(values));
		System.out.println(StatUtils.percentile(values, 0.25));
		System.out.println(StatUtils.percentile(values, 0.55));
		System.out.println(StatUtils.percentile(values, 0.75));
		System.out.println(StatUtils.max(values));
	}
	
	public static void main(String[] args) {
		CentralityAnalysis.testHScore();
//		int a[] = new int[]{1, 2, 3, 4, 5};
//		int b[] = Arrays.copyOfRange(a, 0, 4);
//		for (int i: b) System.out.println(i);
	}
}
