import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;

public class CentralityAnalysis {
	double values[];
	int index;
	double nTotalPath;
	Map<String, Double> nodeCentrality;
	double nodePerLocation[];
	Set<String> visited;
	
	CentralityAnalysis(CallDAG callDAG) {
		getCentralityValues(callDAG);
	}
	
	void getCentralityValues(CallDAG callDAG) {
		nTotalPath = 0;
		nodePerLocation = new double[100 + 1];
		for (String s: callDAG.numOfLeafPath.keySet()) {
			double nPath = callDAG.numOfLeafPath.get(s) * callDAG.numOfRootPath.get(s);
			if (!callDAG.callFrom.containsKey(s)) { // is a root
				nTotalPath += nPath;
				nodePerLocation[(int)(callDAG.location.get(s) * 100)]++;
			}
		}
				
		nodeCentrality = new TreeMap();
		for (String s: callDAG.location.keySet()) {
			double loc = callDAG.location.get(s);
			double nPath = callDAG.numOfLeafPath.get(s) * callDAG.numOfRootPath.get(s);
			double centrality = nPath / nTotalPath;
			nodeCentrality.put(s, centrality);
		}
	}
	
	void getLocationVsAvgCentrality(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("Results//loc-vs-avg-centrality-" + filePath + ".txt"));
		
		Map<Double, Double> avgLocationCentrality = new TreeMap();
		for (String s: callDAG.location.keySet()) {
			double loc = callDAG.location.get(s);
			double centrality = nodeCentrality.get(s);
			
			if (avgLocationCentrality.containsKey(loc)) {
				double c = (avgLocationCentrality.get(loc) + centrality ) / 2.0;
				avgLocationCentrality.put(loc, c);
			}
			else {
				avgLocationCentrality.put(loc, centrality);
			}			
		}
		
		for (double d: avgLocationCentrality.keySet()) {
			pw.println(d + "\t" + avgLocationCentrality.get(d));
		}
		
		pw.close();
	}
	
	void getLocationCentralityScatter(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("Results//loc-vs-centrality" + filePath + ".txt"));
		
		for (String s: nodeCentrality.keySet()) {
			double loc = callDAG.location.get(s);
			double centrality = nodeCentrality.get(s);
			pw.println(loc + "\t" + centrality);
		}
		
		pw.close();
	}
	
	void getCentralityCDF(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("Results//centrality-cdf-" + filePath + ".txt"));
		
		Map<Double, Double> centralityFrequency = new TreeMap();
		for (String s: callDAG.location.keySet()) {
			double centrality = nodeCentrality.get(s);
			if (centralityFrequency.containsKey(centrality)) {
				double v = centralityFrequency.get(centrality);
				centralityFrequency.put(centrality, v + 1.0);
			}
			else {
				centralityFrequency.put(centrality, 1.0);
			}
		}
		
		double sum = 0;
		for (double d: centralityFrequency.keySet()) {
			sum += centralityFrequency.get(d);
			pw.println(d + "\t" + (sum / callDAG.location.size()));
		}
		
		pw.close();
	}
	
	void traverse(String node, CallDAG callDAG) {
		if (!callDAG.callTo.containsKey(node)) {
			values[index++] = nodeCentrality.get(node);
			return;
		}
		
		visited.add(node);
		values[index++] = nodeCentrality.get(node);
		
		boolean flag = false;
		for (String s: callDAG.callTo.get(node)) {
			if (visited.contains(s)) continue;
			traverse(s, callDAG);
			flag = true;
			break;
		}
		
		if (!flag) {
			for (String s: callDAG.callTo.get(node)) {
				traverse(s, callDAG);
				break;
			}
		}
	}
	
//	void plotSamplePath(CallDAG) {
//	}
	
	void getSamplePathKurtosis(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//path-kurtosis-cdf-" + filePath + ".txt"));
		
		double kSum = 0;
		double smallPathKnt = 0;
		visited = new HashSet();
		double arr[][] = new double[20][20];
		int idx = 0;
		
		for (String s: callDAG.location.keySet()) {
			if (callDAG.callFrom.containsKey(s)) continue;
			
			values = new double[callDAG.location.size()];
			index = 0;
			traverse(s, callDAG);
			
			double weightValues[] = new double[index];
			double weightFrequencies[] = new double[index];
			int weightIndex = 0;
			double minCentrality = StatUtils.min(values, 0, index);
			double maxTimes = 0;
			for (int i = 0; i < index; ++i) {
				double f = values[i] / minCentrality;
				weightValues[weightIndex] = i + 1;
				weightFrequencies[weightIndex] = f;
				++weightIndex;
			}
			double k = getKurtosis(weightValues, weightFrequencies);	
			if (Double.isNaN(k)) continue;
			if (Double.isInfinite(k)) continue;
			pw.println(k);
			
//			kSum += k;
			
//			if (index == 11 && idx < 20) {
//				for (int i = 0; i < index; ++i) {
//					arr[idx][i] = values[i];
//				}
//				idx++;
//			}
		}
		
//		System.out.println(idx);
//		
//		for (int j = 0; j < 11; ++j) {
//			for (int i = 0; i < 20; ++i) {
//				System.out.print(arr[i][j] + "\t");
//			}
//			System.out.println();
//		}
		
//		System.out.println(visited.size() + "\t" + callDAG.location.size() + "\t" + smallPathKnt);
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
	
	void getSamplePathHScore(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//path-hscore-cdf-" + filePath + ".txt"));
		
		visited = new HashSet();
		for (String s: callDAG.location.keySet()) {
			if (callDAG.callFrom.containsKey(s)) continue;
			
			values = new double[callDAG.location.size()];
			index = 0;
			traverse(s, callDAG);			
			if (index < 3) continue; // avoid paths of less than 3 hops
			
			double hScore = getHScore(values);
			if (Double.isInfinite(hScore) || Double.isNaN(hScore)) {
				for (double d: values) {
					System.out.println(d + "\t");
				}
				System.out.println();
				continue;
			}
			pw.println(hScore);		
		}
	}
	
	void getSamplePathCentralityRange(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//path-centrality-range-cdf-" + filePath + ".txt"));
		
		visited = new HashSet();
		for (String s: callDAG.location.keySet()) {
			if (callDAG.callFrom.containsKey(s)) continue;
			
			values = new double[callDAG.location.size()];
			index = 0;
			traverse(s, callDAG);			
			if (index < 3) continue; // avoid paths of less than 3 hops
			
			double maxCentrality = StatUtils.max(values);
			double centralityRange = Math.min(maxCentrality - values[0], maxCentrality - values[values.length - 1]);
			pw.println(centralityRange);		
		}
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
				if (values[j] < values[i]) ++disagree;
				else ++agree;
			}
		}
		
		double n = maxIndex + 1;
		double nPairs = (n * (n - 1)) / 2;
		double increasingScore = (agree - disagree) / nPairs;
//		System.out.println(increasingScore);

		
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
		double decreasingScore = (agree - disagree) / nPairs;
//		System.out.println(decreasingScore);
		
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
		double values1[] = {1, 2, 3, 1, 2, 1};
		System.out.println(getHScore(values1));
	}
	
//	public static void main(String[] args) {
//		CentralityAnalysis.testHScore();
//	}
}
