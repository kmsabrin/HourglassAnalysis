import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;

public class CentralityAnalysis {
	double pathCentralityValues[];
	int pathHopCount;
	double nTotalPath;
	HashMap<String, Double> nodeCentrality;
	HashMap<String, Double> nodePathThrough;
	HashMap<String, Double> nodeChildPathSum;
	HashSet<String> visitedNodes;	
	double minRootCentrality;
	double maxRootCentrality;
	
	Random random;
	
	CentralityAnalysis(CallDAG callDAG) {
		nodeCentrality = new HashMap();
		nodePathThrough = new HashMap();
		nodeChildPathSum = new HashMap();
		getCentralityValues(callDAG);
	}
	
	private void getCentralityValues(CallDAG callDAG) {
		nTotalPath = 0;
		for (String s: callDAG.location.keySet()) {
			double nPath = callDAG.numOfLeafPath.get(s) * callDAG.numOfRootPath.get(s);
			nodePathThrough.put(s, nPath);
			if (!callDAG.callFrom.containsKey(s)) { // is a root
				nTotalPath += nPath;
			}
		}
		
		TreeSet<Double> sortedRootCentralities = new TreeSet();
		for (String s: callDAG.location.keySet()) {
			double centrality = nodePathThrough.get(s) / nTotalPath;
			nodeCentrality.put(s, centrality);
//			if (callDAG.callTo.containsKey(s)) System.out.println(s + "\t" + centrality);
			if (!callDAG.callFrom.containsKey(s)) { // is a root
				sortedRootCentralities.add(centrality);
			}
			if (callDAG.callTo.containsKey(s)) { // is not a leaf
				double childPathSum = 0;
				for (String r: callDAG.callTo.get(s)) {
					childPathSum += nodePathThrough.get(r);
				}
				nodeChildPathSum.put(s, childPathSum);
			}
		}		

		minRootCentrality = sortedRootCentralities.first();
		maxRootCentrality = sortedRootCentralities.last();
		System.out.println(nTotalPath + "\t" + (nTotalPath * 0.000005));
	}
	
	// for average and scatter
	void getLocationVsCentrality(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("Results//loc-vs-centrality" + filePath + ".txt"));
		
		for (String s: nodeCentrality.keySet()) {
			pw.println(callDAG.location.get(s) + "\t" + nodeCentrality.get(s));
		}
		
		pw.close();
	}
	
	void getCentralityCDF(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("Results//centrality-cdf-" + filePath + ".txt"));
		
		for (String s: callDAG.location.keySet()) {
			pw.println(nodeCentrality.get(s));	
		}
		
		pw.close();
	}
	
	void traverse(String node, CallDAG callDAG) {
		if (!callDAG.callTo.containsKey(node)) {
			pathCentralityValues[pathHopCount++] = nodeCentrality.get(node); // should put ZERO
			return;
		}
		
		pathCentralityValues[pathHopCount++] = nodeCentrality.get(node);		
		
		String functions[] = callDAG.callTo.get(node).toArray(new String[callDAG.callTo.get(node).size()]);
//		while(true) {
//			String s = functions[random.nextInt(functions.length)];
//			if ((nodePathThrough.get(s)/nodeChildPathSum.get(node)) > random.nextDouble()) {
//				traverse(s, callDAG);
//				break;
//			}
//		}
		
		double rn = random.nextDouble();
		double cumulativeSum = 0;
		for (String s: functions) {
			cumulativeSum += nodePathThrough.get(s)/nodeChildPathSum.get(node);
			if (rn < cumulativeSum) {
				traverse(s, callDAG);
				break;
			}
		}
	}
		
	void getSamplePathStatistics(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw1 = new PrintWriter(new File("Results//path-hscore-cdf-" + filePath + ".txt"));
		PrintWriter pw2 = new PrintWriter(new File("Results//path-centrality-range-cdf-" + filePath + ".txt"));
		PrintWriter pw3 = new PrintWriter(new File("Results//path-kurtosis-cdf-" + filePath + ".txt"));
		PrintWriter pw4 = new PrintWriter(new File("Results//hscore-centralityrange-" + filePath + ".txt"));

		random = new Random(System.nanoTime());
		int samplePathCount = 0;
		int samplePathSize = 1*1000000;
//		int samplePathSize = 2*5;
		double pathLength[] = new double[samplePathSize];
		double pathHScore[] = new double[samplePathSize];
		double pathCentralityRange[] = new double[samplePathSize];
		double pathMaxCentrality[] = new double[samplePathSize];
		
		while (samplePathCount < samplePathSize) {
			for (String s : callDAG.location.keySet()) {
				if (callDAG.callFrom.containsKey(s))
					continue; // start from roots only to trace a path
				
				double rootCentralityScaled = (nodeCentrality.get(s) - minRootCentrality) / (maxRootCentrality - minRootCentrality);
				if (random.nextDouble() > rootCentralityScaled)
					continue; // choose root nodes where nodes with large path count has higher probability of getting selected

				pathCentralityValues = new double[10000]; // max possible path length (assumed)
				pathHopCount = 0;
				traverse(s, callDAG); // traverse the path

				if (pathHopCount < 3)
					continue; // avoid paths of less than 3 hops
				double pathValues[] = Arrays.copyOfRange(pathCentralityValues, 0, pathHopCount); 
				/** make root and leaf zero !!!! **/
				pathValues[0] = pathValues[pathValues.length - 1] = 0;
				
//				for (double d: pathValues) {
//					System.out.print(d + "\t");
//				}
//				System.out.println();

				/*****************************************/
				/*********** get path H-Score ************/
				/*****************************************/
				double hScore = getHScore(pathValues);
				pw1.println(hScore);
			
				/*****************************************/
				/******* get path Centrality Range *******/
				/*****************************************/
				double maxCentrality = StatUtils.max(pathValues);
//				double centralityRange = Math.min(maxCentrality - pathValues[0], maxCentrality- pathValues[pathValues.length - 1]);
//				pw2.println(centralityRange);
				pw2.println(maxCentrality);
				
				pw4.println(hScore + "\t" + maxCentrality);
			
				/*****************************************/
				pathLength[samplePathCount] = pathHopCount;
				pathHScore[samplePathCount] = hScore;
				pathCentralityRange[samplePathCount] = maxCentrality; //centralityRange; CHANGED!
				pathMaxCentrality[samplePathCount] = maxCentrality;
				++samplePathCount;
				if (samplePathCount >= samplePathSize) break;
				
				/*****************************************/
				/********** get path Kurtosis ************/
				/*****************************************/
//				double weightValues[] = new double[index];
//				double weightFrequencies[] = new double[index];
//				int weightIndex = 0;
//				double minCentrality = StatUtils.min(values, 0, index);
//				for (int i = 0; i < index; ++i) {
//					double f = values[i] / minCentrality;
//					weightValues[weightIndex] = i + 1;
//					weightFrequencies[weightIndex] = f;
//					++weightIndex;
//				}
//				double k = getKurtosis(weightValues, weightFrequencies);
//				if (!Double.isNaN(k) && !Double.isInfinite(k)) {
//					pw3.println(k);
//				}
			}
		}
		
//		System.out.println(new PearsonsCorrelation().correlation(pathLength, pathHScore));
//		System.out.println(new PearsonsCorrelation().correlation(pathLength, pathCentralityRange));
//		System.out.println(new PearsonsCorrelation().correlation(pathHScore, pathCentralityRange));
//		System.out.println(new PearsonsCorrelation().correlation(pathHScore, pathMaxCentrality));
		
		pw1.close();
		pw2.close();
		pw3.close();
		pw4.close();
	}
	
	static double getKurtosis(double[] values, double frequencies[]) {
		double n = 0;
		double xf = 0;
		double xxf = 0;
		for (int i = 0; i < values.length; ++i) {
			xf += values[i] * frequencies[i];
			xxf += values[i] * values[i] * frequencies[i];
			n += frequencies[i];
		}
		double mean = xf / n;
		
		double ssx = xxf - (xf * xf / n);
		double std = Math.sqrt(ssx / (n - 1));
		
		double kurt = 0;
		for (int i = 0; i < values.length; ++i) {
			kurt += Math.pow(values[i] - mean, 4.0) * frequencies[i];
		}
		kurt /= Math.pow(std, 4.0);
		double kurt2 = ((n * (n + 1 )) / ((n - 1) * (n - 2) * (n - 3)) * kurt) - (3 * (n - 1) * (n - 1) / ((n - 2) * (n - 3)));
		return kurt2;
	}
	
	static double getHScore(double[] values) {
		int maxIndex = 0;
		double maxCentrality = 0;
		
		for (int i = 0; i < values.length; ++i) {
			if (values[i] > maxCentrality) {
				maxIndex = i;
				maxCentrality = values[i];
			}
		}
		
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
		
		return (increasingScore + decreasingScore) / 2.0;
	}
	
	static void testKurtosis() {
		double values1[] = {1, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 5};
		System.out.println(new Kurtosis().evaluate(values1));
		
		double values2[] = {1, 2, 3, 4, 5};
		double frequencies[] = {1, 2, 5, 3, 1};
		System.out.println(getKurtosis(values2, frequencies));
	}
	
	static void testHScore() {
//		double values1[] = {1, 2, 3, 2, 1};
		double values1[] = {4.6213516589140647E-10,1.4219654823765308E-10,2.2779034562169096E-8,2.2913256043626685E-5,1.718494204546398E-5,0.06930266591684534};
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
			sum += nodeCentrality.get(s);
			values[indx++] = nodeCentrality.get(s);
		}
		System.out.println(indx);
		System.out.println(StatUtils.min(values));
		System.out.println(StatUtils.percentile(values, 0.25));
		System.out.println(StatUtils.percentile(values, 0.55));
		System.out.println(StatUtils.percentile(values, 0.75));
		System.out.println(StatUtils.max(values));
	}
	
	public static void main(String[] args) {
//		CentralityAnalysis.testHScore();
		
		int a[] = new int[]{1, 2, 3, 4, 5};
		int b[] = Arrays.copyOfRange(a, 0, 4);
		for (int i: b) System.out.println(i);
	}
}
